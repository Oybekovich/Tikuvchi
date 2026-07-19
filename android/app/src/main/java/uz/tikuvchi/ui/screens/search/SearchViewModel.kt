package uz.tikuvchi.ui.screens.search

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
import uz.tikuvchi.data.SearchQuery
import uz.tikuvchi.data.matches
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.UstaCard
import uz.tikuvchi.data.model.UstaSearchRow
import uz.tikuvchi.data.reloadOnReconnect

data class SearchUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val query: SearchQuery = SearchQuery(),
    val categories: List<ServiceCategory> = emptyList(),
    val districts: List<String> = emptyList(),
    val results: List<UstaCard> = emptyList(),
    val filtersOpen: Boolean = false,
)

class SearchViewModel(initialText: String, initialCategory: Long?) : ViewModel() {
    private val _state = MutableStateFlow(
        SearchUiState(query = SearchQuery(text = initialText, categoryId = initialCategory)),
    )
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    /** Server tomondan kelgan xom qatorlar — filtr o'zgarganda qayta so'ralmaydi. */
    private var rows: List<UstaSearchRow> = emptyList()

    init {
        load()
        // Tarmoq qaytganda yoki boshqa ekranda "Qayta urinish" bosilganda
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun setText(v: String) = updateQuery { it.copy(text = v) }
    fun setCategory(v: Long?) = updateQuery { it.copy(categoryId = v) }
    fun setMinPrice(v: Long?) = updateQuery { it.copy(minPrice = v) }
    fun setMaxPrice(v: Long?) = updateQuery { it.copy(maxPrice = v) }
    fun toggleFilters() = _state.update { it.copy(filtersOpen = !it.filtersOpen) }

    fun reset() {
        _state.update { it.copy(query = SearchQuery()) }
        load()
    }

    /** Tuman va reyting server tomonda filtrlanadi — ular o'zgarsa qayta so'raymiz. */
    fun setDistrict(v: String?) {
        _state.update { it.copy(query = it.query.copy(district = v)) }
        load()
    }

    fun setMinRating(v: Double?) {
        _state.update { it.copy(query = it.query.copy(minRating = v)) }
        load()
    }

    private fun updateQuery(f: (SearchQuery) -> SearchQuery) {
        _state.update { s ->
            val q = f(s.query)
            s.copy(query = q, results = rows.filter { matches(it, q) }.map { it.toCard() })
        }
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val q = _state.value.query
                // coroutineScope SHART — HomeViewModel'dagi bilan bir sabab:
                // usiz async xatosi catch'ni chetlab o'tib ilovani yiqitadi
                val (loadedRows, categories, districts) = coroutineScope {
                    val ustasAsync = async { CatalogRepository.searchUstas(q.district, q.minRating) }
                    val categoriesAsync = async { CatalogRepository.categories() }
                    val districtsAsync = async { CatalogRepository.districts() }
                    Triple(ustasAsync.await(), categoriesAsync.await(), districtsAsync.await())
                }
                rows = loadedRows
                _state.update { s ->
                    s.copy(
                        loading = false,
                        categories = categories,
                        districts = districts,
                        results = rows.filter { matches(it, s.query) }.map { it.toCard() },
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}
