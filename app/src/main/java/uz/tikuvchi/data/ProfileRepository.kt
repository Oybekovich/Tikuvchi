package uz.tikuvchi.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import uz.tikuvchi.data.model.Profile

object ProfileRepository {

    /** Kirgan foydalanuvchining id'si. Sessiya bo'lmasa — null. */
    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    fun currentEmail(): String = supabase.auth.currentUserOrNull()?.email.orEmpty()

    suspend fun me(): Profile? = withContext(Dispatchers.IO) {
        val id = currentUserId() ?: return@withContext null
        supabase.from("profiles")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()
    }

    suspend fun update(fullName: String, phone: String?) = withContext(Dispatchers.IO) {
        val id = currentUserId() ?: error("Sessiya yo'q")
        supabase.from("profiles")
            .update(ProfileUpdate(fullName = fullName, phone = phone)) {
                filter { eq("id", id) }
            }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) { supabase.auth.signOut() }

    /** Faqat o'zgaradigan maydonlar yuboriladi. */
    @Serializable
    private data class ProfileUpdate(
        @kotlinx.serialization.SerialName("full_name") val fullName: String,
        val phone: String?,
    )
}
