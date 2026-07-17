package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.tikuvchi.data.model.OrderRow
import uz.tikuvchi.data.model.ServiceItem

object OrdersRepository {

    /**
     * Katalog orqali buyurtma. Web'dagi OrderWizard bilan bir xil: avval order,
     * keyin order_item yoziladi va yangi buyurtmaning id'si qaytadi.
     */
    suspend fun create(
        ustaId: String,
        service: ServiceItem,
        material: String?,
        modelNote: String?,
        sizeNote: String?,
        readyDate: String,
    ): String = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")

        val orderId = supabase.from("orders")
            .insert(
                OrderInsert(
                    clientId = clientId,
                    ustaId = ustaId,
                    totalPrice = service.basePrice,
                    estimatedReadyAt = readyDate,
                ),
            ) { select(Columns.list("id")) }
            .decodeSingle<IdRow>()
            .id

        supabase.from("order_items").insert(
            OrderItemInsert(
                orderId = orderId,
                title = service.title,
                material = material?.trim()?.ifEmpty { null },
                sizeNote = sizeNote?.trim()?.ifEmpty { null },
                modelNote = modelNote?.trim()?.ifEmpty { null },
                price = service.basePrice,
            ),
        )
        orderId
    }

    @Serializable
    private data class IdRow(val id: String)

    @Serializable
    private data class OrderInsert(
        @SerialName("client_id") val clientId: String,
        @SerialName("usta_id") val ustaId: String,
        val source: String = "catalog",
        val status: String = "pending",
        @SerialName("total_price") val totalPrice: Long,
        @SerialName("payment_status") val paymentStatus: String = "pending",
        @SerialName("estimated_ready_at") val estimatedReadyAt: String,
    )

    @Serializable
    private data class OrderItemInsert(
        @SerialName("order_id") val orderId: String,
        val title: String,
        val material: String?,
        @SerialName("size_note") val sizeNote: String?,
        @SerialName("model_note") val modelNote: String?,
        val price: Long,
    )

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
