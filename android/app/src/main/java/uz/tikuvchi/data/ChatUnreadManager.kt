package uz.tikuvchi.data

import android.content.Context
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject

object ChatUnreadStore {
    private const val PREFS_NAME = "chat_unread"
    private const val KEY_READ_MAP = "read_map"

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getReadMap(): MutableMap<String, String> {
        val raw = prefs?.getString(KEY_READ_MAP, null) ?: return mutableMapOf()
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            for (key in obj.keys()) {
                map[key] = obj.getString(key)
            }
            map
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun saveReadMap(map: Map<String, String>) {
        val obj = JSONObject(map as Map<String, Any>)
        prefs?.edit()?.putString(KEY_READ_MAP, obj.toString())?.apply()
    }

    fun markRead(conversationId: String) {
        val map = getReadMap()
        map[conversationId] = java.time.Instant.now().toString()
        saveReadMap(map)
    }

    fun getLastRead(conversationId: String): String =
        getReadMap()[conversationId] ?: "2020-01-01T00:00:00Z"
}

suspend fun fetchUnreadConversations(): List<String> = withContext(Dispatchers.IO) {
    val userId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
    val convs = supabase.from("conversations")
        .select(Columns.raw("id")) {
            filter {
                or {
                    eq("client_id", userId)
                    eq("usta_id", userId)
                }
            }
        }
        .decodeList<IdRow>()
    if (convs.isEmpty()) return@withContext emptyList()

    val convIds = convs.map { it.id }

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

    convIds.filter { id ->
        val latest = latestPerConv[id] ?: return@filter false
        val lastRead = ChatUnreadStore.getLastRead(id)
        latest > lastRead
    }
}

@Serializable
private data class IdRow(val id: String)

@Serializable
private data class MsgTimestamp(
    @kotlinx.serialization.SerialName("conversation_id") val conversationId: String,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String,
)
