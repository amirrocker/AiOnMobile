package de.ams.techday.aionmobilemediapipe.llmchat.ui

import de.ams.techday.aionmobilemediapipe.llmchat.model.ChatMessage

const val USER_PREFIX = "user"
const val MODEL_PREFIX = "model"

typealias MessageId = String

interface UiState {
    val messages: List<ChatMessage>
    val fullPrompt: String

    fun createLoadingMessage(): MessageId

    fun appendMessage(id: String, text:String, done: Boolean = false)

    fun addMessage(text: String, author: String): MessageId

}