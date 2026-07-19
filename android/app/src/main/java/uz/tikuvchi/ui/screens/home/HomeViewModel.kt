package uz.tikuvchi.ui.screens.home

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
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.UstaCard
import uz.tikuvchi.data.reloadOnReconnect

data class HomeUiState(
    val loading: Boolean = true,
    val categories: List<ServiceCategory> = emptyList(),
    val ustas: List<UstaCard> = emptyList(),
    val error: Boolean = false,
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
        // Tarmoq qaytganda yoki boshqa ekranda "Qayta urinish" bosilganda
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                // Web'dagi kabi ikkalasi parallel olinadi.
                // coroutineScope SHART: usiz async ichidagi xato ota-coroutine'ni
                // bekor qilib, quyidagi catch'dan yon aylanib o'tadi va ilova
                // yiqiladi (internet uzilganda aynan shunday bo'lgan edi).
                val loaded = coroutineScope {
                    val categories = async { CatalogRepository.categories() }
                    val ustas = async { CatalogRepository.featuredUstas() }
                    HomeUiState(
                        loading = false,
                        categories = categories.await(),
                        ustas = ustas.await(),
                    )
                }
                _state.update { loaded }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}
