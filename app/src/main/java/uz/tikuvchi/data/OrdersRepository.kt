package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.tikuvchi.data.model.OrderRow

object OrdersRepository {

    /** Web'dagi kabi: faol va tugallangan holatlar ajratilgan. */
    private val ACTIVE = listOf("pending", "accepted", "in_progress", "ready")
    private val FINISHED = listOf("completed", "cancelled")

    suspend fun myOrders(finished: Boolean): List<OrderRow> = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
        supabase.from("orders")
            .select(
                Columns.raw(
                    "id, status, payment_status, total_price, estimated_ready_at, " +
                        "created_at, source, " +
                        "usta_profiles!inner(profiles!inner(full_name, avatar_url)), " +
                        "order_items(title)"
                )
            ) {
                filter {
                    eq("client_id", clientId)
                    isIn("status", if (finished) FINISHED else ACTIVE)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }
}
