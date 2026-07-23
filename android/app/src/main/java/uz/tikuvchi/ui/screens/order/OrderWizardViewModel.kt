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
import uz.tikuvchi.data.ChatRepository
import uz.tikuvchi.data.MeasurementsRepository
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
    val newLabel: String = "",
    val newChest: String = "",
    val newWaist: String = "",
    val newHips: String = "",
    val newHeight: String = "",
    val newShoulder: String = "",
    val newSleeveLength: String = "",
    val sizeNote: String = "",
    val suggestedPrice: String = "",
    val submitting: Boolean = false,
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
            if (s.measurementId == NEW_MEASUREMENT && s.newLabel.isBlank()) {
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

    private fun addMeasurementDetails(list: MutableList<String>, m: Measurement) {
        m.chest?.let { list.add("Bust: ${it.toInt()} sm") }
        m.waist?.let { list.add("Bel: ${it.toInt()} sm") }
        m.hips?.let { list.add("Biklar: ${it.toInt()} sm") }
        m.height?.let { list.add("Bo'y: ${it.toInt()} sm") }
        m.shoulder?.let { list.add("Yelka eni: ${it.toInt()} sm") }
        m.sleeveLength?.let { list.add("Qol uzunligi: ${it.toInt()} sm") }
    }

    fun submit(onSent: (ustaId: String, ustaName: String) -> Unit) {
        val s = _state.value
        if (s.submitting) return
        _state.update { it.copy(submitting = true, error = null) }

        viewModelScope.launch {
            try {
                // Save new measurement if entered
                var measurementLabel = ""
                val measurementDetails = mutableListOf<String>()
                if (s.measurementId == NEW_MEASUREMENT) {
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
                    measurementLabel = saved.label
                    addMeasurementDetails(measurementDetails, saved)
                } else {
                    val m = s.measurements.firstOrNull { it.id == s.measurementId }
                    if (m != null) {
                        measurementLabel = m.label
                        addMeasurementDetails(measurementDetails, m)
                    }
                }

                // Build template message
                val lines = mutableListOf("\uD83D\uDCCB Yangi buyurtma taklifi:")
                if (s.material.isNotBlank()) lines.add("\u2022 Mato: ${s.material}")
                if (s.modelNote.isNotBlank()) lines.add("\u2022 Model: ${s.modelNote}")
                lines.add("\u2022 O\u2018lcham: $measurementLabel")
                if (measurementDetails.isNotEmpty()) {
                    lines.addAll(measurementDetails)
                }
                if (s.sizeNote.isNotBlank()) lines.add("\u2022 Izoh: ${s.sizeNote}")
                if (s.suggestedPrice.isNotBlank()) {
                    lines.add("\u2022 Taklif qilingan narx: ${s.suggestedPrice} so\u2018m")
                }

                val conversationId = ChatRepository.ensureConversation(ustaId)
                ChatRepository.sendText(conversationId, lines.joinToString("\n"))

                _state.update { it.copy(submitting = false) }
                onSent(ustaId, _state.value.ustaName)
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = WizardError.GENERIC) }
            }
        }
    }
}
