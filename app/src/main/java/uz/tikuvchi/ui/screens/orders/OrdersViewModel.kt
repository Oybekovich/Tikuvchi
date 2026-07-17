package uz.tikuvchi.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.OrdersRepository
import uz.tikuvchi.data.model.OrderRow

data class OrdersUiState(
    val loading: Boolean = true,
    val finished: Boolean = false,
    val orders: List<OrderRow> = emptyList(),
    val error: Boolean = false,
)

class OrdersViewModel : ViewModel() {
    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    init { load() }

    fun setFinished(v: Boolean) {
        if (_state.value.finished == v) return
        _state.update { it.copy(finished = v) }
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val orders = OrdersRepository.myOrders(_state.value.finished)
                _state.update { it.copy(loading = false, orders = orders) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}
