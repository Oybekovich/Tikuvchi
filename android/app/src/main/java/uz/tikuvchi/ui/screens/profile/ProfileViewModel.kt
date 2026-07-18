package uz.tikuvchi.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.data.model.UserRole
import uz.tikuvchi.util.PHONE_PREFIX
import uz.tikuvchi.util.formatPhone

data class ProfileUiState(
    val loading: Boolean = true,
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CLIENT,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: Boolean = false,
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    fun setFullName(v: String) = _state.update { it.copy(fullName = v, saved = false) }
    fun setPhone(v: String) = _state.update { it.copy(phone = v, saved = false) }

    private fun load() {
        viewModelScope.launch {
            try {
                val p = ProfileRepository.me()
                _state.update {
                    it.copy(
                        loading = false,
                        email = ProfileRepository.currentEmail(),
                        fullName = p?.fullName.orEmpty(),
                        // DB'da raqam formatlanmagan bo'lishi mumkin (seed
                        // ma'lumotlari shunday) — maydonda bir xil ko'rinsin
                        phone = formatPhone(p?.phone.orEmpty()),
                        role = p?.role ?: UserRole.CLIENT,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun save() {
        val s = _state.value
        if (s.saving || s.fullName.isBlank()) return
        _state.update { it.copy(saving = true, saved = false, error = false) }
        viewModelScope.launch {
            try {
                // Faqat prefiks qolgan bo'lsa raqam kiritilmagan hisoblanadi
                val phone = s.phone.trim().takeUnless { it == PHONE_PREFIX.trim() || it.isEmpty() }
                ProfileRepository.update(s.fullName.trim(), phone)
                _state.update { it.copy(saving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, error = true) }
            }
        }
    }

    /** Chiqqach sessionStatus o'zgaradi va TikuvchiRoot o'zi login'ga qaytaradi. */
    fun logout() {
        viewModelScope.launch {
            try {
                ProfileRepository.signOut()
            } catch (e: Exception) {
                _state.update { it.copy(error = true) }
            }
        }
    }
}
