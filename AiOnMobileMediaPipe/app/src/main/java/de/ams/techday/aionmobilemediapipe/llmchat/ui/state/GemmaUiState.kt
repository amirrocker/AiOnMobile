package de.ams.techday.aionmobilemediapipe.llmchat.ui.state

import androidx.compose.runtime.toMutableStateList
import de.ams.techday.aionmobilemediapipe.llmchat.model.ChatMessage
import de.ams.techday.aionmobilemediapipe.llmchat.ui.MODEL_PREFIX
import de.ams.techday.aionmobilemediapipe.llmchat.ui.MessageId
import de.ams.techday.aionmobilemediapipe.llmchat.ui.UiState

class GemmaUiState(
    messages: List<ChatMessage> = emptyList()
) : UiState {

    private val START_TURN = "<start_of_turn>"
    private val END_TURN = "<end_of_turn>"
    private val lock = Any()

    private val _messages: MutableList<ChatMessage> = messages.toMutableStateList()
    override val messages: List<ChatMessage>
        get() = synchronized(lock) {
            _messages.apply {
                for(i in indices) {
                    this[i] = this[i].copy(
                        rawMessage = this[i]
                            .rawMessage.replace("$START_TURN ${this[i].author}\n", "")
                            .replace(END_TURN, "")
                    )
                }
            }.asReversed()
        }

    override val fullPrompt: String
        get() = _messages.takeLast(4).joinToString(separator = "\n") {
            it.rawMessage
        }

    override fun createLoadingMessage(): MessageId {
        val chatMessage = ChatMessage(author = MODEL_PREFIX, isLoading = true)
        _messages.add(chatMessage)
        return chatMessage.id
    }

    fun appendFirstMessage(id: String, text: String) {
        appendMessage(id, "$START_TURN$MODEL_PREFIX\n$text", false)
    }

    override fun appendMessage(id: String, text: String, done: Boolean) {
        val index = _messages.indexOfFirst { it.id == id }
        if(index != -1) {
            val newText = if(done) {
                "${_messages[index].rawMessage}$text$END_TURN"
            } else {
                "${_messages[index].rawMessage}$text"
            }
            _messages[index] = _messages[index].copy(rawMessage = newText, isLoading = false)
        }
    }

    override fun addMessage(text: String, author: String): MessageId  =
        ChatMessage(
            rawMessage = "$author\n$text",
            author = author
        ).also {
            _messages.add(it)
        }.id

}