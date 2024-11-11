package de.ams.techday.aionmobilelitert.ui.presentation

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
import de.ams.techday.aionmobilelitert.ui.theme.AiOnMobileLiteRtTheme

@Composable
fun HomeScreen(
    onSuperResolutionClick: () -> Unit = {},
    onReinforcementLearningClick: () -> Unit = {},
    onTextClassificationClick: () -> Unit = {}
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
                onTextClassificationClick()
            }) {
                Text("Text Classification")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                onSuperResolutionClick()
            }) {
                Text("Super Resolution")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                onReinforcementLearningClick()
            }) {
                Text("Reinforcement Learning")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AiOnMobileLiteRtTheme {
        HomeScreen()
    }
}