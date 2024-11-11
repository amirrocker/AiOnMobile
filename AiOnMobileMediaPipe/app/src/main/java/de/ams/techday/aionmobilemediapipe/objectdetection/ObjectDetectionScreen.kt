package de.ams.techday.aionmobilemediapipe.objectdetection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.ams.techday.aionmobilemediapipe.objectdetection.camerax.CameraView
import de.ams.techday.aionmobilemediapipe.objectdetection.composables.AiOnMobileObjectDetectionBanner
import de.ams.techday.aionmobilemediapipe.objectdetection.composables.TabsTopBar
import de.ams.techday.aionmobilemediapipe.objectdetection.gallery.GalleryView

@Composable
fun ObjectDetectionScreen(
    onOptionsButtonClick: () -> Unit,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int
) {

    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var inferenceTime by rememberSaveable {
        mutableIntStateOf(0)
    }

    Column {

        AiOnMobileObjectDetectionBanner(
            onOptionsClick = onOptionsButtonClick
        )

        TabsTopBar(
            selectedTabIndex = selectedTabIndex,
            setSelectedTabIndex = {
                selectedTabIndex = it
                inferenceTime = 0
            }
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when(selectedTabIndex) {
                0 -> CameraView(
                    threshold = threshold,
                    maxResults = maxResults,
                    delegate = delegate,
                    mlModel = mlModel,
                    setInferenceTime = { inferenceTime = it },
                )
                1 -> GalleryView(
                    threshold = threshold,
                    maxResults = maxResults,
                    delegate = delegate,
                    mlModel = mlModel,
                    setInferenceTime = { inferenceTime = it },
                )
            }
        }
        Box(modifier = Modifier.padding(10.dp)) {
            Text(text = "Inference Time: $inferenceTime ms")
        }
    }
}