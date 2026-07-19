package uz.tikuvchi.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.OrdersRepository
import uz.tikuvchi.data.model.OrderDetail
import uz.tikuvchi.data.reloadOnReconnect

data class OrderDetailUiState(
    val loading: Boolean = true,
    val order: OrderDetail? = null,
    val error: Boolean = false,
)

class OrderDetailViewModel(private val orderId: String) : ViewModel() {
    private val _state = MutableStateFlow(OrderDetailUiState())
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    init {
        load()
        // Tarmoq qaytganda yoki boshqa ekranda "Qayta urinish" bosilganda
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val order = OrdersRepository.detail(orderId)
                _state.update { it.copy(loading = false, order = order) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            try {
                OrdersRepository.cancel(orderId)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = true) }
            }
        }
    }
}
