package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.tikuvchi.data.model.ServiceCategory
import uz.tikuvchi.data.model.UstaCard

/**
 * Katalog: kategoriyalar va ustalar. So'rovlar web ilovadagilar bilan bir xil —
 * ikkala platformada bir xil ro'yxat ko'rinishi shart.
 */
object CatalogRepository {

    private const val USTA_CARD_COLUMNS =
        "user_id, district, rating_avg, rating_count, tags, " +
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
}
