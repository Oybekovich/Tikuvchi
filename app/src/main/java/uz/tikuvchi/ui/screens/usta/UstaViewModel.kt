package uz.tikuvchi.ui.screens.usta

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
import uz.tikuvchi.data.model.ReviewItem
import uz.tikuvchi.data.model.UstaDetail

data class UstaUiState(
    val loading: Boolean = true,
    val usta: UstaDetail? = null,
    val reviews: List<ReviewItem> = emptyList(),
    /** Usta topilmadi yoki tarmoq xatosi — ikkalasi ham "ochib bo'lmadi" */
    val error: Boolean = false,
)

class UstaViewModel(private val ustaId: String) : ViewModel() {
    private val _state = MutableStateFlow(UstaUiState())
    val state: StateFlow<UstaUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                // coroutineScope'siz async xatosi catch'ni chetlab o'tadi
                val (usta, reviews) = coroutineScope {
                    val ustaAsync = async { CatalogRepository.usta(ustaId) }
                    val reviewsAsync = async { CatalogRepository.reviews(ustaId) }
                    ustaAsync.await() to reviewsAsync.await()
                }
                _state.update {
                    if (usta == null) {
                        it.copy(loading = false, error = true)
                    } else {
                        UstaUiState(loading = false, usta = usta, reviews = reviews)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}
