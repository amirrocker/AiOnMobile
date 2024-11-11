package de.ams.techday.aionmobilelitert.superresolution.sample

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class ImageSampleUiState(
    val sampleUriList:List<String> = listOf(
        "lr-1.jpg", "lr-2.jpg", "lr-3.jpg"
    ),
    val selectedBitmap: Bitmap? = null,
    val sharpenedBitmap: Bitmap? = null,
    val inferenceTime: Int = 0
)