package uz.tikuvchi.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.CatalogRepository
import uz.tikuvchi.data.OrdersRepository
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.data.model.OrderRow

data class DashboardUiState(
    val loading: Boolean = true,
    val activeOrders: Int = 0,
    val pendingOrders: Int = 0,
    val totalEarnings: Long = 0,
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val recentPending: List<OrderRow> = emptyList(),
    val unreadChats: Int = 0,
    val error: Boolean = false,
)

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val userId = ProfileRepository.currentUserId() ?: return@launch

                val stats = OrdersRepository.dashboardStats()
                val profile = CatalogRepository.usta(userId)

                _state.update {
                    it.copy(
                        loading = false,
                        activeOrders = stats.activeOrders,
                        pendingOrders = stats.pendingOrders,
                        totalEarnings = stats.totalEarnings,
                        ratingAvg = profile?.ratingAvg ?: 0.0,
                        ratingCount = profile?.ratingCount ?: 0,
                        recentPending = stats.recentPending,
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun acceptOrder(id: String) {
        viewModelScope.launch {
            try {
                OrdersRepository.accept(id)
                load()
            } catch (_: Exception) {}
        }
    }

    fun rejectOrder(id: String) {
        viewModelScope.launch {
            try {
                OrdersRepository.reject(id)
                load()
            } catch (_: Exception) {}
        }
    }
}
