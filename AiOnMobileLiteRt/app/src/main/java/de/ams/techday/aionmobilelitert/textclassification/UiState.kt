package de.ams.techday.aionmobilelitert.textclassification

import androidx.compose.runtime.Immutable

@Immutable
data class UiState(
    val positivePercentage: Float = 0f,
    val negativePercentage: Float = 0f,
    val inferenceTime: Long = 0L,
    val errorMessage: String? = null
)