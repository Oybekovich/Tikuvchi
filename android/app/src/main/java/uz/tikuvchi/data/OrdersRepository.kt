package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uz.tikuvchi.data.model.OrderDetail
import uz.tikuvchi.data.model.OrderRow

object OrdersRepository {

    suspend fun create(
        ustaId: String,
        title: String,
        price: Long,
        material: String?,
        modelNote: String?,
        sizeNote: String?,
    ): String = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")

        val orderId = supabase.from("orders")
            .insert(
                OrderInsert(
                    clientId = clientId,
                    ustaId = ustaId,
                    totalPrice = price,
                ),
            ) { select(Columns.list("id")) }
            .decodeSingle<IdRow>()
            .id

        supabase.from("order_items").insert(
            OrderItemInsert(
                orderId = orderId,
                title = title.ifBlank { "Buyurtma" },
                material = material?.trim()?.ifEmpty { null },
                sizeNote = sizeNote?.trim()?.ifEmpty { null },
                modelNote = modelNote?.trim()?.ifEmpty { null },
                price = price,
            ),
        )
        orderId
    }

    suspend fun accept(id: String) = withContext(Dispatchers.IO) {
        supabase.from("orders")
            .update(StatusUpdate("accepted")) { filter { eq("id", id) } }
    }

    suspend fun cancel(id: String) = withContext(Dispatchers.IO) {
        supabase.from("orders")
            .update(StatusUpdate("cancelled")) { filter { eq("id", id) } }
    }

    suspend fun reject(id: String) = withContext(Dispatchers.IO) {
        val prev = detail(id)?.status?.name?.lowercase() ?: "pending"
        supabase.from("orders")
            .update(StatusUpdate("cancelled")) { filter { eq("id", id) } }
        insertEvent(id, prev, "cancelled")
    }

    suspend fun progressStatus(id: String, toStatus: String) = withContext(Dispatchers.IO) {
        val prev = detail(id)?.status?.name?.lowercase() ?: return@withContext
        supabase.from("orders")
            .update(StatusUpdate(toStatus)) { filter { eq("id", id) } }
        insertEvent(id, prev, toStatus)
    }

    suspend fun advancePayment(id: String, newStatus: String) = withContext(Dispatchers.IO) {
        supabase.from("orders")
            .update(PaymentStatusUpdate(newStatus)) { filter { eq("id", id) } }
    }

    private suspend fun insertEvent(orderId: String, from: String, to: String) {
        val userId = ProfileRepository.currentUserId() ?: return
        runCatching {
            supabase.from("order_events").insert(
                OrderEventInsert(
                    orderId = orderId,
                    fromStatus = from,
                    toStatus = to,
                    changedBy = userId,
                )
            )
        }
    }

    data class DashboardData(
        val activeOrders: Int,
        val pendingOrders: Int,
        val totalEarnings: Long,
        val recentPending: List<OrderRow>,
    )

    suspend fun dashboardStats(): DashboardData = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: return@withContext DashboardData(0, 0, 0, emptyList())

        val allOrders = supabase.from("orders")
            .select(
                Columns.raw(
                    "id, status, payment_status, total_price, estimated_ready_at, " +
                        "created_at, source, usta_id, client_id, " +
                        "usta_profiles!inner(profiles!inner(full_name, avatar_url)), " +
                        "order_items(title)"
                )
            ) {
                filter { eq("usta_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<OrderRow>()

        val active = allOrders.count { it.status.name.lowercase() in listOf("accepted", "in_progress") }
        val pending = allOrders.filter { it.status.name.lowercase() == "pending" }
        val earnings = allOrders
            .filter { it.status.name.lowercase() in listOf("ready", "completed") }
            .sumOf { it.totalPrice }

        DashboardData(
            activeOrders = active,
            pendingOrders = pending.size,
            totalEarnings = earnings,
            recentPending = pending.take(5),
        )
    }

    suspend fun detail(id: String): OrderDetail? = withContext(Dispatchers.IO) {
        supabase.from("orders")
            .select(
                Columns.raw(
                    "id, status, payment_status, source, total_price, " +
                        "estimated_ready_at, created_at, usta_id, " +
                        "usta_profiles!inner(district, profiles!inner(full_name, avatar_url)), " +
                        "order_items(id, title, material, image_url, size_note, model_note, price)"
                )
            ) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull()
    }

    @Serializable
    private data class StatusUpdate(val status: String)

    @Serializable
    private data class PaymentStatusUpdate(@SerialName("payment_status") val paymentStatus: String)

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
    )

    @Serializable
    private data class OrderEventInsert(
        @SerialName("order_id") val orderId: String,
        @SerialName("from_status") val fromStatus: String,
        @SerialName("to_status") val toStatus: String,
        @SerialName("changed_by") val changedBy: String,
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

    private val ACTIVE = listOf("pending", "accepted", "in_progress", "ready")
    private val FINISHED = listOf("completed", "cancelled")

    suspend fun myOrders(finished: Boolean): List<OrderRow> = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
        supabase.from("orders")
            .select(
                Columns.raw(
                    "id, status, payment_status, total_price, estimated_ready_at, " +
                        "created_at, source, usta_id, client_id, " +
                        "usta_profiles!inner(profiles!inner(full_name, avatar_url)), " +
                        "order_items(title)"
                )
            ) {
                filter {
                    or {
                        eq("client_id", userId)
                        eq("usta_id", userId)
                    }
                    isIn("status", if (finished) FINISHED else ACTIVE)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }
}
