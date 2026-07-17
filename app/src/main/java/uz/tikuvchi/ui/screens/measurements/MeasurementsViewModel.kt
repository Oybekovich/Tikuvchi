package uz.tikuvchi.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.MeasurementsRepository
import uz.tikuvchi.data.model.Measurement

/** Formadagi qiymatlar matn ko'rinishida turadi — bo'sh maydon null bo'lib saqlanadi. */
data class MeasurementForm(
    val id: String? = null,
    val label: String = "",
    val chest: String = "",
    val waist: String = "",
    val hips: String = "",
    val height: String = "",
    val shoulder: String = "",
    val sleeveLength: String = "",
    val notes: String = "",
) {
    fun toModel() = Measurement(
        id = id,
        label = label.trim(),
        chest = chest.toDoubleOrNull(),
        waist = waist.toDoubleOrNull(),
        hips = hips.toDoubleOrNull(),
        height = height.toDoubleOrNull(),
        shoulder = shoulder.toDoubleOrNull(),
        sleeveLength = sleeveLength.toDoubleOrNull(),
        notes = notes.trim().ifEmpty { null },
    )

    companion object {
        fun from(m: Measurement) = MeasurementForm(
            id = m.id,
            label = m.label,
            chest = m.chest.text(),
            waist = m.waist.text(),
            hips = m.hips.text(),
            height = m.height.text(),
            shoulder = m.shoulder.text(),
            sleeveLength = m.sleeveLength.text(),
            notes = m.notes.orEmpty(),
        )

        /** 90.0 emas, 90 ko'rinishida — o'lchov butun bo'lsa kasr qismi ko'rsatilmaydi. */
        private fun Double?.text(): String = when {
            this == null -> ""
            this % 1.0 == 0.0 -> toLong().toString()
            else -> toString()
        }
    }
}

data class MeasurementsUiState(
    val loading: Boolean = true,
    val items: List<Measurement> = emptyList(),
    val form: MeasurementForm? = null,
    val saving: Boolean = false,
    val error: Boolean = false,
)

class MeasurementsViewModel : ViewModel() {
    private val _state = MutableStateFlow(MeasurementsUiState())
    val state: StateFlow<MeasurementsUiState> = _state.asStateFlow()

    init { load() }

    fun openNew() = _state.update { it.copy(form = MeasurementForm(), error = false) }
    fun openEdit(m: Measurement) =
        _state.update { it.copy(form = MeasurementForm.from(m), error = false) }
    fun closeForm() = _state.update { it.copy(form = null) }
    fun editForm(f: (MeasurementForm) -> MeasurementForm) =
        _state.update { s -> s.copy(form = s.form?.let(f)) }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val items = MeasurementsRepository.list()
                _state.update { it.copy(loading = false, items = items) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun save() {
        val form = _state.value.form ?: return
        if (_state.value.saving || form.label.isBlank()) return
        _state.update { it.copy(saving = true, error = false) }
        viewModelScope.launch {
            try {
                val model = form.toModel()
                if (form.id == null) {
                    MeasurementsRepository.insert(model)
                } else {
                    MeasurementsRepository.update(model)
                }
                _state.update { it.copy(saving = false, form = null) }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, error = true) }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                MeasurementsRepository.delete(id)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = true) }
            }
        }
    }
}
