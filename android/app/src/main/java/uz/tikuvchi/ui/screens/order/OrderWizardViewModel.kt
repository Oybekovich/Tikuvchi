package uz.tikuvchi.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.CatalogRepository
import uz.tikuvchi.data.MeasurementsRepository
import uz.tikuvchi.data.OrdersRepository
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.data.model.Measurement

enum class WizardError { SELECT_MEASUREMENT, GENERIC }

const val NEW_MEASUREMENT = "new"

data class OrderWizardUiState(
    val loading: Boolean = true,
    val step: Int = 0,
    val ustaName: String = "",
    val measurements: List<Measurement> = emptyList(),
    val material: String = "",
    val modelNote: String = "",
    val measurementId: String? = null,
    val showNewForm: Boolean = false,
    val newLabel: String = "",
    val newChest: String = "",
    val newWaist: String = "",
    val newHips: String = "",
    val newHeight: String = "",
    val newShoulder: String = "",
    val newSleeveLength: String = "",
    val suggestedPrice: String = "",
    val sizeNote: String = "",
    val submitting: Boolean = false,
    val savingNew: Boolean = false,
    val error: WizardError? = null,
)

class OrderWizardViewModel(private val ustaId: String) : ViewModel() {
    private val _state = MutableStateFlow(OrderWizardUiState())
    val state: StateFlow<OrderWizardUiState> = _state.asStateFlow()

    init { load() }

    fun setMaterial(v: String) = _state.update { it.copy(material = v) }
    fun setModelNote(v: String) = _state.update { it.copy(modelNote = v) }
    fun setSuggestedPrice(v: String) = _state.update { it.copy(suggestedPrice = v) }
    fun setMeasurement(id: String) = _state.update { it.copy(measurementId = id, error = null) }
    fun setNewLabel(v: String) = _state.update { it.copy(newLabel = v, error = null) }
    fun setNewChest(v: String) = _state.update { it.copy(newChest = v) }
    fun setNewWaist(v: String) = _state.update { it.copy(newWaist = v) }
    fun setNewHips(v: String) = _state.update { it.copy(newHips = v) }
    fun setNewHeight(v: String) = _state.update { it.copy(newHeight = v) }
    fun setNewShoulder(v: String) = _state.update { it.copy(newShoulder = v) }
    fun setNewSleeveLength(v: String) = _state.update { it.copy(newSleeveLength = v) }
    fun setSizeNote(v: String) = _state.update { it.copy(sizeNote = v) }

    fun showNewMeasurementForm() {
        _state.update { it.copy(showNewForm = true, measurementId = NEW_MEASUREMENT, error = null) }
    }

    fun hideNewMeasurementForm() {
        val s = _state.value
        _state.update {
            it.copy(
                showNewForm = false,
                measurementId = it.measurements.firstOrNull()?.id,
            )
        }
    }

    fun saveNewMeasurement(onSaved: () -> Unit) {
        val s = _state.value
        if (s.savingNew) return
        _state.update { it.copy(savingNew = true, error = null) }

        viewModelScope.launch {
            try {
                val fields = mapOf(
                    "chest" to s.newChest,
                    "waist" to s.newWaist,
                    "hips" to s.newHips,
                    "height" to s.newHeight,
                    "shoulder" to s.newShoulder,
                    "sleeve_length" to s.newSleeveLength,
                )
                val nonNull = fields.mapValues { it.value.toDoubleOrNull() }
                val saved = MeasurementsRepository.insert(
                    Measurement(
                        label = s.newLabel.trim(),
                        chest = nonNull["chest"],
                        waist = nonNull["waist"],
                        hips = nonNull["hips"],
                        height = nonNull["height"],
                        shoulder = nonNull["shoulder"],
                        sleeveLength = nonNull["sleeve_length"],
                    ),
                )
                _state.update {
                    it.copy(
                        savingNew = false,
                        showNewForm = false,
                        measurementId = saved.id,
                        measurements = listOf(saved) + it.measurements,
                        newLabel = "",
                        newChest = "",
                        newWaist = "",
                        newHips = "",
                        newHeight = "",
                        newShoulder = "",
                        newSleeveLength = "",
                    )
                }
                onSaved()
            } catch (e: Exception) {
                _state.update { it.copy(savingNew = false, error = WizardError.GENERIC) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val (usta, measurements) = coroutineScope {
                    val ustaAsync = async { CatalogRepository.usta(ustaId) }
                    val measurementsAsync = async { MeasurementsRepository.list() }
                    ustaAsync.await() to measurementsAsync.await()
                }
                _state.update {
                    it.copy(
                        loading = false,
                        ustaName = usta?.profiles?.fullName.orEmpty(),
                        measurements = measurements,
                        measurementId = measurements.firstOrNull()?.id,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = WizardError.GENERIC) }
            }
        }
    }

    private fun validate(): WizardError? {
        val s = _state.value
        if (s.step == 1) {
            if (s.measurementId == null) return WizardError.SELECT_MEASUREMENT
            if (s.measurementId == NEW_MEASUREMENT && !s.showNewForm) {
                return WizardError.SELECT_MEASUREMENT
            }
        }
        return null
    }

    fun next() {
        val problem = validate()
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(2), error = null) }
    }

    fun back(): Boolean {
        val s = _state.value
        if (s.step == 0) return true
        _state.update { it.copy(step = it.step - 1, error = null) }
        return false
    }

    fun submit(onCreated: (orderId: String) -> Unit) {
        val s = _state.value
        if (s.submitting) return
        _state.update { it.copy(submitting = true, error = null) }

        viewModelScope.launch {
            try {
                val price = s.suggestedPrice.filter { it.isDigit() }.toLongOrNull() ?: 0

                val orderId = OrdersRepository.create(
                    ustaId = ustaId,
                    title = "Buyurtma",
                    price = price,
                    material = s.material.trim().ifEmpty { null },
                    modelNote = s.modelNote.trim().ifEmpty { null },
                    sizeNote = s.sizeNote.trim().ifEmpty { null },
                )

                _state.update { it.copy(submitting = false) }
                onCreated(orderId)
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = WizardError.GENERIC) }
            }
        }
    }
}
