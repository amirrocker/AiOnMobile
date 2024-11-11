package de.ams.techday.aionmobilemediapipe.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.ams.techday.aionmobilemediapipe.ui.theme.AiOnMobileMediaPipeTheme

@Composable
fun HomeScreen(
    onObjectDetectionClick: () -> Unit = {},
    onLlmChatScreenClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                onObjectDetectionClick()
            }) {
                Text("Object Detection")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                onLlmChatScreenClick()
            }) {
                Text("Llm Chat Screen")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AiOnMobileMediaPipeTheme {
        HomeScreen()
    }
}