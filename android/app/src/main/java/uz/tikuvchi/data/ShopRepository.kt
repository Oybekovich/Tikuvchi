package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.tikuvchi.data.model.GenderSegment
import uz.tikuvchi.data.model.ServiceItem
import uz.tikuvchi.data.model.ServiceCategory

object ShopRepository {

    suspend fun myProfile(): UstaProfileEdit? = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: return@withContext null
        supabase.from("usta_profiles")
            .select(
                Columns.raw(
                    "user_id, bio, cover_image_url, location_text, district, " +
                        "work_hours_start, work_hours_end, tags, gender_segment, available, visible, " +
                        "profiles!inner(full_name, avatar_url)"
                )
            ) {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull()
    }

    suspend fun updateProfile(
        bio: String?,
        locationText: String?,
        district: String?,
        workHoursStart: String?,
        workHoursEnd: String?,
        tags: List<String>,
        genderSegment: GenderSegment?,
        available: Boolean?,
        visible: Boolean?,
    ) = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")
        supabase.from("usta_profiles").update(
            ProfileUpdate(
                bio = bio?.trim()?.ifEmpty { null },
                locationText = locationText?.trim()?.ifEmpty { null },
                district = district?.trim()?.ifEmpty { null },
                workHoursStart = workHoursStart?.trim()?.ifEmpty { null },
                workHoursEnd = workHoursEnd?.trim()?.ifEmpty { null },
                tags = tags.ifEmpty { null },
                genderSegment = genderSegment,
                available = available,
                visible = visible,
            )
        ) { filter { eq("user_id", userId) } }
    }

    suspend fun services(): List<ServiceItem> = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
        supabase.from("usta_services")
            .select(Columns.raw("id, title, description, base_price")) {
                filter { eq("usta_id", userId) }
            }
            .decodeList()
    }

    suspend fun addService(
        categoryId: Long?,
        title: String,
        description: String?,
        basePrice: Long,
    ) = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")
        supabase.from("usta_services").insert(
            ServiceInsert(
                ustaId = userId,
                categoryId = categoryId,
                title = title,
                description = description?.trim()?.ifEmpty { null },
                basePrice = basePrice,
            )
        ) { select(Columns.list("id")) }
            .decodeSingle<IdRow>()
            .id
    }

    suspend fun updateService(
        id: Long,
        categoryId: Long?,
        title: String,
        description: String?,
        basePrice: Long,
    ) = withContext(Dispatchers.IO) {
        supabase.from("usta_services").update(
            ServiceUpdate(
                categoryId = categoryId,
                title = title,
                description = description?.trim()?.ifEmpty { null },
                basePrice = basePrice,
            )
        ) { filter { eq("id", id) } }
    }

    suspend fun deleteService(id: Long) = withContext(Dispatchers.IO) {
        supabase.from("usta_services").delete { filter { eq("id", id) } }
    }

    suspend fun categories(): List<ServiceCategory> = withContext(Dispatchers.IO) {
        supabase.from("service_categories")
            .select(Columns.list("id", "name", "icon", "gender_segment"))
            .decodeList()
    }

    @Serializable
    private data class IdRow(val id: Long)

    @Serializable
    private data class ProfileUpdate(
        val bio: String? = null,
        @SerialName("location_text") val locationText: String? = null,
        val district: String? = null,
        @SerialName("work_hours_start") val workHoursStart: String? = null,
        @SerialName("work_hours_end") val workHoursEnd: String? = null,
        val tags: List<String>? = null,
        @SerialName("gender_segment") val genderSegment: GenderSegment? = null,
        val available: Boolean? = null,
        val visible: Boolean? = null,
        @SerialName("cover_image_url") val coverImageUrl: kotlinx.serialization.json.JsonNull? = null,
    )

    @Serializable
    private data class ServiceInsert(
        @SerialName("usta_id") val ustaId: String,
        @SerialName("category_id") val categoryId: Long? = null,
        val title: String,
        val description: String? = null,
        @SerialName("base_price") val basePrice: Long,
    )

    @Serializable
    private data class ServiceUpdate(
        @SerialName("category_id") val categoryId: Long? = null,
        val title: String,
        val description: String? = null,
        @SerialName("base_price") val basePrice: Long,
    )
}

@Serializable
data class UstaProfileEdit(
    @SerialName("user_id") val userId: String,
    val bio: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("location_text") val locationText: String? = null,
    val district: String? = null,
    @SerialName("work_hours_start") val workHoursStart: String? = null,
    @SerialName("work_hours_end") val workHoursEnd: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("gender_segment") val genderSegment: GenderSegment = GenderSegment.WOMEN,
    val available: Boolean = true,
    val visible: Boolean = true,
    val profiles: uz.tikuvchi.data.model.ProfileBrief? = null,
)
