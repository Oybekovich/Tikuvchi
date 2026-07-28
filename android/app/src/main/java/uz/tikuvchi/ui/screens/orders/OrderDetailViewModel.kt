package uz.tikuvchi.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.OrdersRepository
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.data.model.OrderDetail
import uz.tikuvchi.data.reloadOnReconnect

data class OrderDetailUiState(
    val loading: Boolean = true,
    val order: OrderDetail? = null,
    val error: Boolean = false,
    val acting: Boolean = false,
)

class OrderDetailViewModel(private val orderId: String) : ViewModel() {
    private val _state = MutableStateFlow(OrderDetailUiState())
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    init {
        load()
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val order = OrdersRepository.detail(orderId)
                _state.update { it.copy(loading = false, order = order, acting = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true, acting = false) }
            }
        }
    }

    fun accept() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(acting = true) }
                OrdersRepository.accept(orderId)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(acting = false, error = true) }
            }
        }
    }

    fun cancel() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(acting = true) }
                OrdersRepository.cancel(orderId)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(acting = false, error = true) }
            }
        }
    }

    fun reject() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(acting = true) }
                OrdersRepository.reject(orderId)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(acting = false, error = true) }
            }
        }
    }

    fun progressStatus(toStatus: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(acting = true) }
                OrdersRepository.progressStatus(orderId, toStatus)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(acting = false, error = true) }
            }
        }
    }

    fun advancePayment(newStatus: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(acting = true) }
                OrdersRepository.advancePayment(orderId, newStatus)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(acting = false, error = true) }
            }
        }
    }

    fun isUsta(): Boolean {
        val order = _state.value.order ?: return false
        return ProfileRepository.currentUserId() == order.ustaId
    }
}
