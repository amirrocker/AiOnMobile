package de.ams.techday.aionmobilemediapipe.objectdetection.composables

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.ams.techday.aionmobilemediapipe.R
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectionHelper
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectionHelper.Companion.DELEGATE_CPU
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectionHelper.Companion.DELEGATE_GPU
import de.ams.techday.aionmobilemediapipe.ui.theme.AiOnMobileMediaPipeTheme
import de.ams.techday.aionmobilemediapipe.ui.theme.Turquoise
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    threshold: Float,
    setThreshold: (Float) -> Unit,
    maxResults: Int,
    setMaxResults: (Int) -> Unit,
    delegate: Int,
    setDelegate: (Int) -> Unit,
    mlModel: Int,
    setMlModel: (Int) -> Unit,
    onBackButtonClick: () -> Unit
) {

    // two dropdowns - one for model one for delegate
    var delegateDropdownExpanded by remember { mutableStateOf(false) }
    var mlModelDropdownExpanded by remember { mutableStateOf(false) }

    Column {
        AiOnMobileObjectDetectionBanner(
            onBackButtonClick = onBackButtonClick
        )
        // TODO replace below manual top bar with TopAppBar
//        TopAppBar(
//            title = {
//                Text("title text")
//            },
//            modifier = Modifier
//                .padding(10.dp)
//                .fillMaxWidth(),
//            actions = {}
//        )
        Box(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Options",
                fontSize = 25.sp,
            )
        }
        HorizontalDivider()

        // a series of rows each describing ui for setting a setting

        // region threshold controls
        // First, we have the threshold controls, which has two buttons to
        // decrease and increase threshold value within [0, 0.8] range
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "Threshold",
                modifier = Modifier
                    .weight(1f),
            )
            // Minus button
            IconButton(
                onClick = {
                    // Transforming threshold value to an integer before updating it to
                    // avoid accumulating floating point errors
                    val newThreshold = ((threshold * 10).toInt() - 1).toDouble() / 10

                    setThreshold(
                        max(
                            newThreshold.toFloat(),
                            0.0f,
                        )
                    )
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_minus),
                    contentDescription = null,
                    tint = Turquoise
                )
            }
            Box(
                modifier = Modifier.width(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$threshold".substring(IntRange(0, 2)),
                )
            }
            IconButton(
                onClick = {
                    val newThreshold = ((threshold * 10).toInt() + 1).toDouble() / 10
                    setThreshold(
                        min(
                            newThreshold.toFloat(),
                            0.8f,
                        )
                    )
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_plus),
                    contentDescription = null,
                    tint = Turquoise
                )
            }
        }
        // endregion

        // region max results control
        // Secondly, similar to threshold controls, we have the maxResults controls,
        // which has two buttons to decrease and increase maxResults value within [1, 5] range
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "Max Results",
                modifier = Modifier
                    .weight(1f),
            )
            IconButton(
                onClick = {
                    setMaxResults(
                        max(
                            maxResults - 1,
                            1,
                        )
                    )
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_minus),
                    contentDescription = null,
                    tint = Turquoise
                )
            }
            Box(
                modifier = Modifier.width(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$maxResults",
                )
            }
            IconButton(
                onClick = {
                    setMaxResults(
                        min(
                            maxResults + 1,
                            5,
                        )
                    )
                },
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_plus),
                    contentDescription = null,
                    tint = Turquoise
                )
            }
        }
        // endregion

        // region delegate controls
        // Thirdly, we have the delegate controls which is a dropdown to select one of the
        // two options: DELEGATE_CPU and DELEGATE_GPU
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "Delegate",
                modifier = Modifier
                    .weight(1f),
            )
            Text(
                text = when (delegate) {
                    DELEGATE_CPU  -> "CPU"
                    else -> "GPU"
                },
            )
            IconButton(
                onClick = {
                    delegateDropdownExpanded = true
                },
            ) {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Turquoise
                )
                DropdownMenu(
                    expanded = delegateDropdownExpanded,
                    onDismissRequest = { delegateDropdownExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "CPU") },
                        onClick = {
                            setDelegate(DELEGATE_CPU)
                            delegateDropdownExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = "GPU") },
                        onClick = {
                            setDelegate(DELEGATE_GPU)
                            delegateDropdownExpanded = false
                        },
                    )
                }
            }
        }
        // endregion

        // region mlModel controls
        // Lastly, similar to delegate controls, we have the mlModel controls which is a dropdown to
        // select a number of options: MODEL_EFFICIENTDET V0-V2 and MODEL_EFFICIENTNET V0-V2
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "ML Model",
                modifier = Modifier
                    .weight(1f),
            )
            Text(
                text = when (mlModel) {
                    ObjectDetectionHelper.MODEL_EFFICIENTDETV0 -> "EfficientDet Lite0"
                    ObjectDetectionHelper.MODEL_EFFICIENTDETV1 -> "EfficientDet Lite0"
                    ObjectDetectionHelper.MODEL_EFFICIENTDETV2 -> "EfficientDet Lite0"
                    else -> "Invalid model found".also {
                        Timber.e(IllegalStateException(it))
                    }
                },
            )
            IconButton(
                onClick = {
                    mlModelDropdownExpanded = true
                },
            ) {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Turquoise
                )
                DropdownMenu(
                    expanded = mlModelDropdownExpanded,
                    onDismissRequest = { mlModelDropdownExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "EfficientDet Lite0") },
                        onClick = {
                            setMlModel(ObjectDetectionHelper.MODEL_EFFICIENTDETV0 /*MODEL_EFFICIENT_DET_V0*/)
                            mlModelDropdownExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = "EfficientDet Lite1") },
                        onClick = {
                            setMlModel( ObjectDetectionHelper.MODEL_EFFICIENTDETV1 /*MODEL_EFFICIENT_DET_V2*/)
                            mlModelDropdownExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = "EfficientDet Lite2") },
                        onClick = {
                            setMlModel( ObjectDetectionHelper.MODEL_EFFICIENTDETV2 /*MODEL_EFFICIENT_DET_V2*/)
                            mlModelDropdownExpanded = false
                        },
                    )
                }
            }
        }
        // endregion
    }
}

@Preview(showBackground = true, backgroundColor = Color.WHITE.toLong())
@Composable
fun OptionsScreenPreview() {
    AiOnMobileMediaPipeTheme {
        OptionsScreen(
            threshold = 0.3f,
            {},
            maxResults = 4,
            {},
            delegate = 1,
            {},
            mlModel = 1,
            {},
            {}
        )
    }
}