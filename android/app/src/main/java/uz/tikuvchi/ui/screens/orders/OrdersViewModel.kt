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
import uz.tikuvchi.data.model.OrderRow
import uz.tikuvchi.data.reloadOnReconnect

data class OrdersUiState(
    val loading: Boolean = true,
    val finished: Boolean = false,
    val orders: List<OrderRow> = emptyList(),
    val error: Boolean = false,
    val acting: String? = null,
    val userId: String? = null,
)

class OrdersViewModel : ViewModel() {
    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    init {
        load()
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun setFinished(v: Boolean) {
        if (_state.value.finished == v) return
        _state.update { it.copy(finished = v) }
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val userId = ProfileRepository.currentUserId()
                val orders = OrdersRepository.myOrders(_state.value.finished)
                _state.update { it.copy(loading = false, orders = orders, userId = userId, acting = null) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true, acting = null) }
            }
        }
    }

    fun accept(id: String) {
        _state.update { it.copy(acting = id) }
        viewModelScope.launch {
            try {
                OrdersRepository.accept(id)
                load()
            } catch (_: Exception) {
                _state.update { it.copy(acting = null) }
            }
        }
    }

    fun cancel(id: String) {
        _state.update { it.copy(acting = id) }
        viewModelScope.launch {
            try {
                OrdersRepository.cancel(id)
                load()
            } catch (_: Exception) {
                _state.update { it.copy(acting = null) }
            }
        }
    }

    fun reject(id: String) {
        _state.update { it.copy(acting = id) }
        viewModelScope.launch {
            try {
                OrdersRepository.reject(id)
                load()
            } catch (_: Exception) {
                _state.update { it.copy(acting = null) }
            }
        }
    }

    fun progressStatus(id: String, toStatus: String) {
        _state.update { it.copy(acting = id) }
        viewModelScope.launch {
            try {
                OrdersRepository.progressStatus(id, toStatus)
                load()
            } catch (_: Exception) {
                _state.update { it.copy(acting = null) }
            }
        }
    }
}
