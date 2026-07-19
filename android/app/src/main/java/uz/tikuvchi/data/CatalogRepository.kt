package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import uz.tikuvchi.data.model.ReviewItem
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.UstaCard
import uz.tikuvchi.data.model.UstaDetail
import uz.tikuvchi.data.model.UstaSearchRow

/**
 * Katalog: kategoriyalar va ustalar. So'rovlar web ilovadagilar bilan bir xil —
 * ikkala platformada bir xil ro'yxat ko'rinishi shart.
 */
object CatalogRepository {

    private const val USTA_CARD_COLUMNS =
        "user_id, district, cover_image_url, rating_avg, rating_count, tags, " +
            "profiles!inner(full_name, avatar_url), usta_services(base_price)"

    suspend fun categories(): List<ServiceCategory> = withContext(Dispatchers.IO) {
        supabase.from("service_categories")
            .select(Columns.list("id", "name", "icon", "gender_segment")) {
                filter { eq("gender_segment", "women") }
                order("id", Order.ASCENDING)
            }
            .decodeList()
    }

    /** Bosh sahifadagi ustalar — reytingi bo'yicha. */
    suspend fun featuredUstas(): List<UstaCard> = withContext(Dispatchers.IO) {
        supabase.from("usta_profiles")
            .select(Columns.raw(USTA_CARD_COLUMNS)) {
                order("rating_avg", Order.DESCENDING)
            }
            .decodeList()
    }

    /** Usta sahifasi. Topilmasa — null. */
    suspend fun usta(id: String): UstaDetail? = withContext(Dispatchers.IO) {
        supabase.from("usta_profiles")
            .select(
                Columns.raw(
                    "user_id, bio, cover_image_url, rating_avg, rating_count, " +
                        "location_text, district, work_hours_start, work_hours_end, tags, " +
                        "profiles!inner(full_name, avatar_url), " +
                        "usta_services(id, title, description, base_price), " +
                        "portfolio_items(id, image_url, caption, sort_order)"
                )
            ) {
                filter { eq("user_id", id) }
            }
            .decodeSingleOrNull()
    }

    /**
     * Qidiruv uchun ustalar. Web'dagi kabi faqat tuman va reyting server tomonda
     * filtrlanadi — qolgani (matn, kategoriya, narx) SearchFilter'da.
     */
    suspend fun searchUstas(
        district: String? = null,
        minRating: Double? = null,
    ): List<UstaSearchRow> = withContext(Dispatchers.IO) {
        supabase.from("usta_profiles")
            .select(
                Columns.raw(
                    "user_id, district, bio, cover_image_url, rating_avg, rating_count, tags, " +
                        "profiles!inner(full_name, avatar_url), " +
                        "usta_services(base_price, category_id)"
                )
            ) {
                filter {
                    district?.let { eq("district", it) }
                    minRating?.let { gte("rating_avg", it) }
                }
                order("rating_avg", Order.DESCENDING)
            }
            .decodeList()
    }

    /** Filtr uchun tumanlar ro'yxati — takrorlanmagan va tartiblangan. */
    suspend fun districts(): List<String> = withContext(Dispatchers.IO) {
        supabase.from("usta_profiles")
            .select(Columns.list("district")) {
                filter { filterNot("district", FilterOperator.IS, "null") }
            }
            .decodeList<DistrictRow>()
            .mapNotNull { it.district }
            .distinct()
            .sorted()
    }

    @Serializable
    private data class DistrictRow(val district: String? = null)

    suspend fun reviews(ustaId: String): List<ReviewItem> = withContext(Dispatchers.IO) {
        supabase.from("reviews")
            .select(
                Columns.raw(
                    "id, rating, comment, created_at, " +
                        "profiles!reviews_client_id_fkey(full_name, avatar_url)"
                )
            ) {
                filter { eq("usta_id", ustaId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }
}
