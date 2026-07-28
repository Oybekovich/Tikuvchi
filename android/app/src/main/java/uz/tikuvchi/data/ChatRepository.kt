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
import uz.tikuvchi.data.model.MessagePreview
import uz.tikuvchi.data.model.ProfileBrief

object ChatRepository {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun conversations(): List<ConversationWithProfile> = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
        val rows = supabase.from("conversations")
            .select(
                Columns.raw(
                    "id, client_id, usta_id, last_message_at, " +
                        "usta_profiles!inner(profiles!inner(full_name, avatar_url)), " +
                        "messages(content, message_type, created_at)"
                )
            ) {
                filter {
                    or {
                        eq("client_id", userId)
                        eq("usta_id", userId)
                    }
                }
                order("last_message_at", Order.DESCENDING)
                order("created_at", Order.DESCENDING, referencedTable = "messages")
                limit(1, referencedTable = "messages")
            }
            .decodeList<ConversationRow>()

        if (rows.isEmpty()) return@withContext emptyList()

        val otherIds = rows.map { row ->
            if (row.clientId == userId) row.ustaId else row.clientId
        }.distinct()

        val others = supabase.from("profiles")
            .select(Columns.raw("id, full_name, avatar_url")) {
                filter {
                    or {
                        otherIds.forEach { eq("id", it) }
                    }
                }
            }
            .decodeList<OtherProfile>()

        val otherMap = others.associateBy { it.id }
        val convIds = rows.map { it.id }

        val msgs = supabase.from("messages")
            .select(Columns.raw("conversation_id, created_at")) {
                filter {
                    neq("sender_id", userId)
                    or {
                        convIds.forEach { eq("conversation_id", it) }
                    }
                }
            }
            .decodeList<MsgTimestamp>()

        val latestPerConv = mutableMapOf<String, String>()
        for (msg in msgs) {
            val prev = latestPerConv[msg.conversationId]
            if (prev == null || msg.createdAt > prev) {
                latestPerConv[msg.conversationId] = msg.createdAt
            }
        }

        rows.map { row ->
            val otherId = if (row.clientId == userId) row.ustaId else row.clientId
            val profile = otherMap[otherId]
            val latest = latestPerConv[row.id]
            val lastRead = ChatUnreadStore.getLastRead(row.id)
            val hasUnread = latest != null && latest > lastRead
            ConversationWithProfile(
                id = row.id,
                clientId = row.clientId,
                ustaId = row.ustaId,
                lastMessageAt = row.lastMessageAt,
                messages = row.messages,
                otherName = profile?.fullName ?: row.usta.profiles?.fullName ?: "",
                otherAvatar = profile?.avatarUrl ?: row.usta.profiles?.avatarUrl,
                unread = hasUnread,
            )
        }
    }

    suspend fun findConversation(otherId: String): String? = withContext(Dispatchers.IO) {
        val userId = ProfileRepository.currentUserId() ?: return@withContext null
        supabase.from("conversations")
            .select(Columns.raw("id, client_id, usta_id")) {
                filter {
                    or {
                        eq("client_id", userId)
                        eq("usta_id", userId)
                    }
                }
            }
            .decodeList<ConversationLookup>()
            .firstOrNull {
                (it.clientId == userId && it.ustaId == otherId) ||
                    (it.clientId == otherId && it.ustaId == userId)
            }?.id
    }

    suspend fun ensureConversation(ustaId: String): String = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")
        supabase.from("conversations")
            .upsert(ConversationInsert(clientId = clientId, ustaId = ustaId)) {
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
    private data class ConversationLookup(
        val id: String,
        @SerialName("client_id") val clientId: String,
        @SerialName("usta_id") val ustaId: String,
    )

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

    @Serializable
    private data class OtherProfile(
        val id: String,
        @SerialName("full_name") val fullName: String,
        @SerialName("avatar_url") val avatarUrl: String? = null,
    )

    @Serializable
    private data class MsgTimestamp(
        @SerialName("conversation_id") val conversationId: String,
        @SerialName("created_at") val createdAt: String,
    )
}

data class ConversationWithProfile(
    val id: String,
    val clientId: String,
    val ustaId: String,
    val lastMessageAt: String,
    val messages: List<MessagePreview>,
    val otherName: String,
    val otherAvatar: String?,
    val unread: Boolean = false,
) {
    val last: MessagePreview? get() = messages.firstOrNull()
}
