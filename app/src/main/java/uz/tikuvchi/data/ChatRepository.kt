package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uz.tikuvchi.data.model.ConversationRow
import uz.tikuvchi.data.model.Message

object ChatRepository {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun conversations(): List<ConversationRow> = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
        supabase.from("conversations")
            .select(
                Columns.raw(
                    "id, usta_id, last_message_at, " +
                        "usta_profiles!inner(profiles!inner(full_name, avatar_url)), " +
                        "messages(content, message_type, created_at)"
                )
            ) {
                filter { eq("client_id", clientId) }
                order("last_message_at", Order.DESCENDING)
                // Ro'yxatda faqat oxirgi xabar ko'rsatiladi
                order("created_at", Order.DESCENDING, referencedTable = "messages")
                limit(1, referencedTable = "messages")
            }
            .decodeList()
    }

    /** Usta bilan mavjud suhbat. Hali yozishilmagan bo'lsa — null. */
    suspend fun findConversation(ustaId: String): String? = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: return@withContext null
        supabase.from("conversations")
            .select(Columns.list("id")) {
                filter {
                    eq("client_id", clientId)
                    eq("usta_id", ustaId)
                }
            }
            .decodeSingleOrNull<IdRow>()?.id
    }

    /** Suhbat yo'q bo'lsa yaratadi — birinchi xabar yuborilganda chaqiriladi. */
    suspend fun ensureConversation(ustaId: String): String = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")
        supabase.from("conversations")
            .upsert(ConversationInsert(clientId = clientId, ustaId = ustaId)) {
                // Suhbat allaqachon bo'lsa yangisi yaratilmaydi — o'shanisi qaytadi
                onConflict = "client_id,usta_id"
                select(Columns.list("id"))
            }
            .decodeSingle<IdRow>()
            .id
    }

    suspend fun messages(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        supabase.from("messages")
            .select {
                filter { eq("conversation_id", conversationId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList()
    }

    suspend fun sendText(conversationId: String, content: String): Message =
        withContext(Dispatchers.IO) {
            val senderId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")
            supabase.from("messages")
                .insert(
                    MessageInsert(
                        conversationId = conversationId,
                        senderId = senderId,
                        content = content,
                    ),
                ) { select() }
                .decodeSingle()
        }

    /**
     * Suhbatdagi o'zgarishlar oqimi: yangi xabarlar (INSERT) va taklif holati
     * o'zgarishlari (UPDATE) — web'dagi ChatWindow obunasi bilan bir xil.
     */
    fun messageChanges(conversationId: String): Flow<Message> {
        val channel = supabase.channel("messages:$conversationId")
        val filter: io.github.jan.supabase.realtime.PostgresChangeFilter.() -> Unit = {
            table = "messages"
            filter("conversation_id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, conversationId)
        }
        val inserts = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public", filter)
            .map { json.decodeFromJsonElement(Message.serializer(), it.record) }
        val updates = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public", filter)
            .map { json.decodeFromJsonElement(Message.serializer(), it.record) }
        return merge(inserts, updates)
    }

    suspend fun subscribe(conversationId: String) =
        supabase.channel("messages:$conversationId").subscribe()

    suspend fun unsubscribe(conversationId: String) {
        supabase.realtime.removeChannel(supabase.channel("messages:$conversationId"))
    }

    @Serializable
    private data class IdRow(val id: String)

    @Serializable
    private data class ConversationInsert(
        @SerialName("client_id") val clientId: String,
        @SerialName("usta_id") val ustaId: String,
    )

    @Serializable
    private data class MessageInsert(
        @SerialName("conversation_id") val conversationId: String,
        @SerialName("sender_id") val senderId: String,
        val content: String,
        @SerialName("message_type") val messageType: String = "text",
    )
}
