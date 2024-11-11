package de.ams.techday.aionmobilemediapipe.objectdetection.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.ams.techday.aionmobilemediapipe.R
import de.ams.techday.aionmobilemediapipe.ui.theme.Turquoise

@Composable
fun AiOnMobileObjectDetectionBanner(
    onOptionsClick: (() -> Unit)? = null,
    onBackButtonClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xEEEEEEEE))
    ) {

        if(onBackButtonClick != null) {
            IconButton(
                onClick = onBackButtonClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back arrow",
                    tint = Turquoise
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.media_pipe_banner),
            contentDescription = "AiOnMobile ObjectDetection logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.Center)
        )
        if(onOptionsClick != null) {
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "settings icon",
                    tint = Turquoise
                )
            }
        }


    }
}