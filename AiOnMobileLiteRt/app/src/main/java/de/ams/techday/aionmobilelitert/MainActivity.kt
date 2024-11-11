package de.ams.techday.aionmobilelitert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.ams.techday.aionmobilelitert.reinforcementlearning.ReinforcementLearningScreen
import de.ams.techday.aionmobilelitert.superresolution.SuperResolutionScreen
import de.ams.techday.aionmobilelitert.textclassification.TextClassificationScreen
import de.ams.techday.aionmobilelitert.ui.presentation.HomeScreen
import de.ams.techday.aionmobilelitert.ui.theme.AiOnMobileLiteRtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiOnMobileLiteRtTheme {
                AiOnMobileLiteRtApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiOnMobileLiteRtApp() {

    AiOnMobileLiteRtTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Ai on mobile litert")
                    }
                )
            }
        ) {
            Surface(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                // impl. serialized navigation
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "Home"
                ) {
                    composable(
                        route = "Home"
                    ) {
                        HomeScreen(
                            onTextClassificationClick = {
                                navController.navigate("TextClassification")
                            },
                            onSuperResolutionClick = {
                                navController.navigate("SuperResolution")
                            },
                            onReinforcementLearningClick = {
                                navController.navigate("ReinforcementLearning")
                            }
                        )
                    }
                    composable(
                        route = "TextClassification"
                    ) {
                        TextClassificationScreen()
                    }
                    composable(
                        route = "SuperResolution"
                    ) {
                        SuperResolutionScreen()
                    }
                    composable(
                        route = "ReinforcementLearning"
                    ) {
                        ReinforcementLearningScreen()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    AiOnMobileLiteRtTheme {
        AiOnMobileLiteRtApp()
    }
}
