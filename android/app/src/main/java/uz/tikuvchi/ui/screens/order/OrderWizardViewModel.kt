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
import uz.tikuvchi.data.model.Measurement
import uz.tikuvchi.data.model.ServiceItem
import java.time.LocalDate

/** Xato turi — matnni ekran strings.xml'dan oladi. */
enum class WizardError { SELECT_SERVICE, SELECT_MEASUREMENT, GENERIC }

/** O'lcham tanlovi: saqlangani yoki yangisi. */
const val NEW_MEASUREMENT = "new"

data class OrderWizardUiState(
    val loading: Boolean = true,
    val step: Int = 0,
    val ustaName: String = "",
    val services: List<ServiceItem> = emptyList(),
    val measurements: List<Measurement> = emptyList(),
    val serviceId: Long? = null,
    val material: String = "",
    val modelNote: String = "",
    val measurementId: String? = null,
    val newMeasurementLabel: String = "",
    val sizeNote: String = "",
    val readyDate: String = LocalDate.now().plusDays(14).toString(),
    val submitting: Boolean = false,
    val error: WizardError? = null,
) {
    val service: ServiceItem? get() = services.firstOrNull { it.id == serviceId }
}

class OrderWizardViewModel(private val ustaId: String) : ViewModel() {
    private val _state = MutableStateFlow(OrderWizardUiState())
    val state: StateFlow<OrderWizardUiState> = _state.asStateFlow()

    init { load() }

    fun setService(id: Long) = _state.update { it.copy(serviceId = id, error = null) }
    fun setMaterial(v: String) = _state.update { it.copy(material = v) }
    fun setModelNote(v: String) = _state.update { it.copy(modelNote = v) }
    fun setMeasurement(id: String) = _state.update { it.copy(measurementId = id, error = null) }
    fun setNewMeasurementLabel(v: String) = _state.update { it.copy(newMeasurementLabel = v, error = null) }
    fun setSizeNote(v: String) = _state.update { it.copy(sizeNote = v) }
    fun setReadyDate(v: String) = _state.update { it.copy(readyDate = v) }

    private fun load() {
        viewModelScope.launch {
            try {
                // coroutineScope'siz async xatosi catch'ni chetlab o'tadi
                val (usta, measurements) = coroutineScope {
                    val ustaAsync = async { CatalogRepository.usta(ustaId) }
                    val measurementsAsync = async { MeasurementsRepository.list() }
                    ustaAsync.await() to measurementsAsync.await()
                }
                _state.update {
                    it.copy(
                        loading = false,
                        ustaName = usta?.profiles?.fullName.orEmpty(),
                        services = usta?.services.orEmpty(),
                        measurements = measurements,
                        // Saqlangani bo'lsa birinchisi tanlangan turadi
                        measurementId = measurements.firstOrNull()?.id,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = WizardError.GENERIC) }
            }
        }
    }

    /** Web'dagi validateStep bilan bir xil. */
    private fun validate(): WizardError? {
        val s = _state.value
        if (s.step == 0 && s.service == null) return WizardError.SELECT_SERVICE
        if (s.step == 1) {
            if (s.measurementId == null) return WizardError.SELECT_MEASUREMENT
            if (s.measurementId == NEW_MEASUREMENT && s.newMeasurementLabel.isBlank()) {
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

    /** true qaytsa — ekrandan chiqish kerak (birinchi bosqichdan orqaga). */
    fun back(): Boolean {
        val s = _state.value
        if (s.step == 0) return true
        _state.update { it.copy(step = it.step - 1, error = null) }
        return false
    }

    fun submit(onCreated: (String) -> Unit) {
        val s = _state.value
        val service = s.service ?: return
        if (s.submitting) return
        _state.update { it.copy(submitting = true, error = null) }

        viewModelScope.launch {
            try {
                // Yangi o'lcham kiritilgan bo'lsa avval uni saqlaymiz
                val label = if (s.measurementId == NEW_MEASUREMENT) {
                    val saved = MeasurementsRepository.insert(
                        Measurement(label = s.newMeasurementLabel.trim()),
                    )
                    saved.label
                } else {
                    s.measurements.firstOrNull { it.id == s.measurementId }?.label.orEmpty()
                }

                val orderId = OrdersRepository.create(
                    ustaId = ustaId,
                    service = service,
                    material = s.material,
                    modelNote = s.modelNote,
                    // Web'dagi kabi: o'lcham nomi va izoh " — " bilan birlashadi
                    sizeNote = listOf(label, s.sizeNote.trim())
                        .filter { it.isNotEmpty() }
                        .joinToString(" — "),
                    readyDate = s.readyDate,
                )
                _state.update { it.copy(submitting = false) }
                onCreated(orderId)
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = WizardError.GENERIC) }
            }
        }
    }
}
