package de.ams.techday.aionmobilemediapipe.llmchat.model

import de.ams.techday.aionmobilemediapipe.llmchat.ui.USER_PREFIX
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val rawMessage: String = "",
    val author: String,
    val isLoading: Boolean = false
) {
    val isFromUser : Boolean
        get() = author == USER_PREFIX
    val message : String
        get() = rawMessage.trim()

    companion object {
        fun asStub() = ChatMessage(
            rawMessage = "RawMessage",
            author = "author"
        )
    }
}

