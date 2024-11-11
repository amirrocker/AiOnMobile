package de.ams.techday.aionmobilemediapipe.llmchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ams.techday.aionmobilemediapipe.llmchat.model.ChatMessage
import de.ams.techday.aionmobilemediapipe.llmchat.ui.UiState
import de.ams.techday.aionmobilemediapipe.llmchat.ui.state.GemmaUiState
import de.ams.techday.aionmobilemediapipe.ui.theme.AiOnMobileMediaPipeTheme

@Composable
fun LlmInferenceScreen(
    llmInferenceViewModel: LlmInferenceViewModel = viewModel(
        factory = LlmInferenceViewModel.getFactory(LocalContext.current.applicationContext)
    )
) {

    val uiState by llmInferenceViewModel.state.collectAsStateWithLifecycle()
    val textInputEnabled by llmInferenceViewModel.textInputEnabled.collectAsStateWithLifecycle()

    LlmInferenceContent(
        uiState,
        textInputEnabled,
    ) {
        llmInferenceViewModel.onSendMessage(it)
    }

}

@Composable
fun LlmInferenceContent(
    uiState: UiState,
    textInputEnabled: Boolean = true,
    onSendMessage: (String) -> Unit
) {

    var userMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(bottom = 56.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(uiState.messages) { chatMessage ->
                ChatItemRenderer(chatMessage)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column { }

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                label = {
                    Text("your input")
                },
                modifier = Modifier
                    .weight(0.85f),
                enabled = textInputEnabled
            )
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f),
                enabled = textInputEnabled
            ) {
                Icon(
                    Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier
                )
            }

        }
    }

}

@Composable
fun ChatItemRenderer(
    chatMessage: ChatMessage
) {

    val horizontalAlignment = if (chatMessage.isFromUser) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val bubbleShape = if (chatMessage.isFromUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }
    val backgroundColor = if (chatMessage.isFromUser) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        val author = if (chatMessage.isFromUser) {
            "user"
        } else {
            "model"
        }

        Text(
            modifier = Modifier
                .padding(bottom = 4.dp),
            text = author,
            style = MaterialTheme.typography.bodySmall
        )
        Row {
            Box {
                Card(
                    colors = CardDefaults.cardColors(contentColor = backgroundColor),
                    modifier = Modifier.fillMaxWidth(.9f)/*.widthIn(0.dp, maxWidth * 0.9f)*/,
                    shape = bubbleShape,
                ) {
                    if (chatMessage.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Text(
                            text = chatMessage.message,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// region previews
@Composable
@Preview
fun ChatItemPreview() {
    AiOnMobileMediaPipeTheme {
        ChatItemRenderer(ChatMessage.asStub())
    }
}

@Composable
@Preview
fun LlmInferenceScreenPreview() {
    AiOnMobileMediaPipeTheme {
        LlmInferenceContent(
            uiState = GemmaUiState(
                messages = listOf(
                    ChatMessage(
                        rawMessage = "hello Max",
                        author = "author",
                        isLoading = false
                    )
                )
            ),
        ) { _ -> println("preview click") }
    }
}
// endregion

