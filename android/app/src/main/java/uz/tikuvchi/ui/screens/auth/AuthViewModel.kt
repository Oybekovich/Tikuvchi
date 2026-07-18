package uz.tikuvchi.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import uz.tikuvchi.data.supabase
import uz.tikuvchi.util.PHONE_PREFIX

enum class AuthMode { LOGIN, REGISTER }

/** Xato turi — matnni ekran o'zi strings.xml'dan oladi, ViewModel resurslarni bilmaydi. */
enum class AuthError { INVALID_CREDENTIALS, EMAIL_TAKEN, GENERIC }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: AuthError? = null,
    val checkEmail: Boolean = false,
)

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setMode(mode: AuthMode) = _state.update { it.copy(mode = mode, error = null, checkEmail = false) }
    fun setFullName(v: String) = _state.update { it.copy(fullName = v) }
    fun setPhone(v: String) = _state.update { it.copy(phone = v) }
    fun setEmail(v: String) = _state.update { it.copy(email = v) }
    fun setPassword(v: String) = _state.update { it.copy(password = v) }

    /**
     * @param onSuccess sessiya paydo bo'lganda chaqiriladi. Ro'yxatdan o'tishda
     * email tasdiqlash yoqilgani uchun sessiya bo'lmasligi mumkin — u holda
     * checkEmail ko'rsatiladi.
     */
    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.busy) return
        _state.update { it.copy(busy = true, error = null, checkEmail = false) }

        viewModelScope.launch {
            try {
                if (s.mode == AuthMode.LOGIN) {
                    supabase.auth.signInWith(Email) {
                        email = s.email.trim()
                        password = s.password
                    }
                    onSuccess()
                } else {
                    // Enter bilan yuborilsa prefiks yolg'iz qolishi mumkin
                    val phone = if (s.phone.trim() == PHONE_PREFIX.trim()) "" else s.phone.trim()
                    supabase.auth.signUpWith(Email) {
                        email = s.email.trim()
                        password = s.password
                        data = buildJsonObject {
                            put("full_name", JsonPrimitive(s.fullName.trim()))
                            put("role", JsonPrimitive("client"))
                            put("phone", if (phone.isEmpty()) JsonPrimitive(null as String?) else JsonPrimitive(phone))
                        }
                    }
                    if (supabase.auth.currentSessionOrNull() == null) {
                        _state.update { it.copy(checkEmail = true) }
                    } else {
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                _state.update {
                    it.copy(
                        error = when {
                            msg.contains("Invalid login credentials", true) -> AuthError.INVALID_CREDENTIALS
                            msg.contains("already registered", true) -> AuthError.EMAIL_TAKEN
                            else -> AuthError.GENERIC
                        }
                    )
                }
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }
}
