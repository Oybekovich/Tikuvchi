package uz.tikuvchi.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.ShopRepository
import uz.tikuvchi.data.UstaProfileEdit
import uz.tikuvchi.data.model.GenderSegment
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.ServiceItem

data class ShopUiState(
    val loading: Boolean = true,
    val profile: UstaProfileEdit? = null,
    val services: List<ServiceItem> = emptyList(),
    val categories: List<ServiceCategory> = emptyList(),
    val error: Boolean = false,
    val saved: Boolean = false,
    val serviceSaved: Boolean = false,
    // Profile form
    val bio: String = "",
    val locationText: String = "",
    val district: String = "",
    val workHoursStart: String = "",
    val workHoursEnd: String = "",
    val tagsText: String = "",
    val genderSegment: GenderSegment = GenderSegment.WOMEN,
    val available: Boolean = true,
    val visible: Boolean = true,
    // Service form
    val showServiceForm: Boolean = false,
    val editingServiceId: Long? = null,
    val serviceTitle: String = "",
    val serviceDescription: String = "",
    val servicePrice: String = "",
    val serviceCategoryId: Long? = null,
)

class ShopViewModel : ViewModel() {
    private val _state = MutableStateFlow(ShopUiState())
    val state: StateFlow<ShopUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val profile = ShopRepository.myProfile()
                val services = ShopRepository.services()
                val cats = ShopRepository.categories()
                _state.update {
                    it.copy(
                        loading = false,
                        profile = profile,
                        services = services,
                        categories = cats,
                        bio = profile?.bio.orEmpty(),
                        locationText = profile?.locationText.orEmpty(),
                        district = profile?.district.orEmpty(),
                        workHoursStart = profile?.workHoursStart.orEmpty(),
                        workHoursEnd = profile?.workHoursEnd.orEmpty(),
                        tagsText = profile?.tags?.joinToString(", ").orEmpty(),
                        genderSegment = profile?.genderSegment ?: GenderSegment.WOMEN,
                        available = profile?.available ?: true,
                        visible = profile?.visible ?: true,
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun setBio(v: String) = _state.update { it.copy(bio = v) }
    fun setLocationText(v: String) = _state.update { it.copy(locationText = v) }
    fun setDistrict(v: String) = _state.update { it.copy(district = v) }
    fun setWorkHoursStart(v: String) = _state.update { it.copy(workHoursStart = v) }
    fun setWorkHoursEnd(v: String) = _state.update { it.copy(workHoursEnd = v) }
    fun setTagsText(v: String) = _state.update { it.copy(tagsText = v) }
    fun setGenderSegment(v: GenderSegment) = _state.update { it.copy(genderSegment = v) }
    fun setAvailable(v: Boolean) = _state.update { it.copy(available = v) }
    fun setVisible(v: Boolean) = _state.update { it.copy(visible = v) }
    fun clearSaved() = _state.update { it.copy(saved = false) }
    fun clearServiceSaved() = _state.update { it.copy(serviceSaved = false) }

    fun saveProfile() {
        viewModelScope.launch {
            try {
                val s = _state.value
                ShopRepository.updateProfile(
                    bio = s.bio,
                    locationText = s.locationText,
                    district = s.district,
                    workHoursStart = s.workHoursStart.ifBlank { null },
                    workHoursEnd = s.workHoursEnd.ifBlank { null },
                    tags = s.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    genderSegment = s.genderSegment,
                    available = s.available,
                    visible = s.visible,
                )
                _state.update { it.copy(saved = true) }
            } catch (_: Exception) {}
        }
    }

    // Service form
    fun showAddService() = _state.update {
        it.copy(showServiceForm = true, editingServiceId = null, serviceTitle = "", serviceDescription = "", servicePrice = "", serviceCategoryId = null)
    }

    fun showEditService(svc: ServiceItem) = _state.update {
        it.copy(showServiceForm = true, editingServiceId = svc.id, serviceTitle = svc.title, serviceDescription = svc.description.orEmpty(), servicePrice = svc.basePrice.toString())
    }

    fun hideServiceForm() = _state.update { it.copy(showServiceForm = false) }

    fun setServiceTitle(v: String) = _state.update { it.copy(serviceTitle = v) }
    fun setServiceDescription(v: String) = _state.update { it.copy(serviceDescription = v) }
    fun setServicePrice(v: String) = _state.update { it.copy(servicePrice = v) }
    fun setServiceCategoryId(v: Long?) = _state.update { it.copy(serviceCategoryId = v) }

    fun saveService() {
        viewModelScope.launch {
            try {
                val s = _state.value
                val price = s.servicePrice.replace(" ", "").toLongOrNull() ?: return@launch
                if (s.editingServiceId != null) {
                    ShopRepository.updateService(
                        id = s.editingServiceId,
                        categoryId = s.serviceCategoryId,
                        title = s.serviceTitle,
                        description = s.serviceDescription,
                        basePrice = price,
                    )
                } else {
                    ShopRepository.addService(
                        categoryId = s.serviceCategoryId,
                        title = s.serviceTitle,
                        description = s.serviceDescription,
                        basePrice = price,
                    )
                }
                val services = ShopRepository.services()
                _state.update { it.copy(showServiceForm = false, services = services, serviceSaved = true) }
            } catch (_: Exception) {}
        }
    }

    fun deleteService(id: Long) {
        viewModelScope.launch {
            try {
                ShopRepository.deleteService(id)
                val services = ShopRepository.services()
                _state.update { it.copy(services = services) }
            } catch (_: Exception) {}
        }
    }
}
