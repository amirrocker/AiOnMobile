package de.ams.techday.aionmobilemediapipe

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.ams.techday.aionmobilemediapipe.llmchat.LlmInferenceScreen
import de.ams.techday.aionmobilemediapipe.llmchat.LoadingScreen
import de.ams.techday.aionmobilemediapipe.objectdetection.ObjectDetectionScreen
import de.ams.techday.aionmobilemediapipe.objectdetection.composables.OptionsScreen
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectionHelper.Companion.DELEGATE_CPU
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectionHelper.Companion.MODEL_EFFICIENTDETV0
import de.ams.techday.aionmobilemediapipe.ui.presentation.HomeScreen
import de.ams.techday.aionmobilemediapipe.ui.theme.AiOnMobileMediaPipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiOnMobileMediaPipeTheme {
                AiOnMobileApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiOnMobileApp() {

    var threshold by rememberSaveable {
        mutableFloatStateOf(0.4f)
    }

    var maxResults by rememberSaveable {
        mutableIntStateOf(5)
    }

    var delegate by rememberSaveable {
        mutableStateOf(DELEGATE_CPU)
    }

    var mlModel by rememberSaveable {
        mutableStateOf( MODEL_EFFICIENTDETV0)
    }

    AiOnMobileMediaPipeTheme {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Ai on mobile with Media Pipe")
                    }
                )
            }
        ) { paddingValues ->

            Surface(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "Home"
                ) {
                    composable(
                        route = "Home"
                    ) {
                        HomeScreen(
                            onObjectDetectionClick = {
                                navController.navigate("ObjectDetection") {
                                    popUpTo("ObjectDetection") {inclusive = true}
                                    launchSingleTop = true
                                }
                            },
                            onLlmChatScreenClick = {
                                navController.navigate("LlmChatLoadingScreen") {
                                    popUpTo("LlmChatLoadingScreen") {inclusive = true}
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(
                        route = "ObjectDetection"
                    ) {
                        ObjectDetectionScreen(
                            onOptionsButtonClick = {
                                navController.navigate("Options") {
                                    popUpTo("Options") {inclusive = true}
                                    launchSingleTop = true
                                }
                            },
                            threshold = threshold,
                            maxResults = maxResults,
                            delegate = delegate,
                            mlModel = mlModel,
                        )
                    }
                    composable(
                        route = "LlmChatLoadingScreen"
                    ) {
                        LoadingScreen(
                            onModelLoaded = {
                                navController.navigate("LlmChatScreen") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(
                        route = "LlmChatScreen"
                    ) {
                        LlmInferenceScreen()
                    }
                    composable(
                        route = "Options"
                    ) {
                        OptionsScreen(
                            onBackButtonClick = {
                                navController.popBackStack()
                            },
                            threshold = threshold,
                            setThreshold = {threshold = it},
                            maxResults = maxResults,
                            setMaxResults = {maxResults = it},
                            delegate = delegate,
                            setDelegate = {delegate = it},
                            mlModel = mlModel,
                            setMlModel = {mlModel = it}
                        )
                    }
                }
            }
        }
    }
}
