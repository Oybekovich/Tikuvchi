package uz.tikuvchi.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikuvchi.data.ChatRepository
import uz.tikuvchi.data.ProfileRepository
import uz.tikuvchi.data.model.ConversationRow
import uz.tikuvchi.data.model.Message
import uz.tikuvchi.data.reloadOnReconnect

data class ChatListUiState(
    val loading: Boolean = true,
    val conversations: List<ConversationRow> = emptyList(),
    val error: Boolean = false,
)

class ChatListViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    init {
        load()
        // Tarmoq qaytganda yoki boshqa ekranda "Qayta urinish" bosilganda
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val list = ChatRepository.conversations()
                _state.update { it.copy(loading = false, conversations = list) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}

data class ChatUiState(
    val loading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val sending: Boolean = false,
    val error: Boolean = false,
    val myId: String = "",
)

class ChatViewModel(private val ustaId: String) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState(myId = ProfileRepository.currentUserId().orEmpty()))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    /** Suhbat birinchi xabar yuborilgunga qadar yaratilmaydi. */
    private var conversationId: String? = null

    init {
        load()
        // Tarmoq qaytganda yoki boshqa ekranda "Qayta urinish" bosilganda
        reloadOnReconnect({ _state.value.error }, ::load)
    }

    fun setInput(v: String) = _state.update { it.copy(input = v) }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                val id = ChatRepository.findConversation(ustaId)
                conversationId = id
                val msgs = if (id != null) ChatRepository.messages(id) else emptyList()
                _state.update { it.copy(loading = false, messages = msgs) }
                if (id != null) observe(id)
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    /** Realtime: yangi xabarlar va taklif holati o'zgarishlari. */
    private fun observe(id: String) {
        viewModelScope.launch {
            try {
                ChatRepository.messageChanges(id).collect { msg ->
                    _state.update { s ->
                        val existing = s.messages.indexOfFirst { it.id == msg.id }
                        s.copy(
                            messages = if (existing >= 0) {
                                s.messages.toMutableList().apply { set(existing, msg) }
                            } else {
                                s.messages + msg
                            },
                        )
                    }
                }
            } catch (e: Exception) {
                // Realtime uzilsa ham chat o'qishga yaroqli — xabar ko'rsatmaymiz
            }
        }
        viewModelScope.launch {
            try {
                ChatRepository.subscribe(id)
            } catch (e: Exception) {
                // obuna bo'lmasa yangi xabarlar o'zi kelmaydi, qolgani ishlayveradi
            }
        }
    }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isEmpty() || _state.value.sending) return
        _state.update { it.copy(sending = true, error = false) }
        viewModelScope.launch {
            try {
                val id = conversationId ?: ChatRepository.ensureConversation(ustaId).also {
                    conversationId = it
                    observe(it)
                }
                val msg = ChatRepository.sendText(id, text)
                _state.update { s ->
                    // Realtime ham shu xabarni qaytarishi mumkin — takrorlanmasin
                    val already = s.messages.any { it.id == msg.id }
                    s.copy(
                        sending = false,
                        input = "",
                        messages = if (already) s.messages else s.messages + msg,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(sending = false, error = true) }
            }
        }
    }

    override fun onCleared() {
        val id = conversationId ?: return
        // ViewModel yopilgach kanal ochiq qolmasin
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            runCatching { ChatRepository.unsubscribe(id) }
        }
    }
}
