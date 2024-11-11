package de.ams.techday.aionmobilemediapipe.objectdetection.camerax

import android.Manifest
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult
import de.ams.techday.aionmobilemediapipe.objectdetection.composables.ResultsOverlay
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectionHelper
import de.ams.techday.aionmobilemediapipe.objectdetection.detection.ObjectDetectorListener
import java.util.concurrent.Executors

/**
 *
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    setInferenceTime: (newInferenceTime:Int) -> Unit
) {

    // handle the camera permission first
    val storagePermissionState: PermissionState =
        rememberPermissionState(Manifest.permission.CAMERA)

    // check permission state and if not yet granted ask for permission for camera
    LaunchedEffect(key1 = Unit) {
        if(storagePermissionState.hasPermission.not()) {
            storagePermissionState.launchPermissionRequest()
        }
    }

    //
    if(storagePermissionState.hasPermission.not()) {
        Text("No Storage Permission granted")
        // bail and do nothing else camera
        return
    }

    //
    var results by remember {
        mutableStateOf<ObjectDetectionResult?>(null)
    }

    // initial state is 1x1 with aspect ratio of 4:3
    var frameHeight by remember { mutableStateOf(4) }
    var frameWidth by remember { mutableStateOf(3) }
    var active by remember { mutableStateOf(true) }

    // setup for camera preview
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // what happens when camera preview is disposed of. close all current cameras
    DisposableEffect(Unit) {
        onDispose {
            active = false
            cameraProviderFuture.get().unbindAll()
        }
    }

    // UI for the preview
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // let getFittedBoxSize do the calculation to fit camera preview
        val cameraPreviewSize = getFittedBoxSize(
            containerSize = Size(
                width = this.maxWidth.value,
                height = this.maxHeight.value
            ),
            boxSize = Size(
                width = frameWidth.toFloat(),
                height = frameHeight.toFloat()
            )
        )

        //
        Box(
            modifier = Modifier
                .width(cameraPreviewSize.width.dp)
                .height(cameraPreviewSize.height.dp)
        ) {
            // use cameraX
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)

                    val executor = ContextCompat.getMainExecutor(context)

                    cameraProviderFuture.addListener({

                        val cameraProvider = cameraProviderFuture.get()

                        // set the surface for the camera
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // specify what camera to use
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        // use ImageAnalyzer to apply transformations to input frame before
                        // feeding it to the object detector
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()

                        // apply object detection. we run it in a single new thread
                        val backgroundExecutor = Executors.newSingleThreadExecutor()

                        // run the object detector
                        backgroundExecutor.execute {
                            // use ObjectDetectorHelper class to abstract away
                            // media pipe specifics from UI
                            val objectDetectionHelper = ObjectDetectionHelper(
                                context = context,
                                threshold = threshold,
                                currentDelegate = delegate,
                                currentModel = mlModel,
                                maxResults = maxResults,

                                objectDetectionListener = ObjectDetectorListener(
                                    onErrorCallback = { error, errorCode ->
                                        println("callback error: $error code: $errorCode")
                                    },
                                    onResultsCallback = { result ->
                                        // on receiving results, we receive the exact
                                        // camera dimensions
                                        frameHeight = result.inputImageHeight
                                        frameWidth = result.inputImageWidth

                                        // is the camera view still active?
                                        if(active) {
                                            results = result.results.first()
                                            setInferenceTime(result.inferenceTime.toInt())
                                        }
                                    }
                                ),
                                runningMode = RunningMode.LIVE_STREAM
                            )

                            // objectDetector instance is now available
                            // set the analyzer and start detecting from livestream
                            imageAnalyzer.setAnalyzer(
                                backgroundExecutor,
                                objectDetectionHelper::detectLiveStreamFrame
                            )
                        }

                        // close any open camera
                        // then open it again using our own to display the live feed
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            imageAnalyzer,
                            preview
                        )
                    }, executor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            // final step is to check for latest results
            // if there are any show overlay
            results?.let {
                ResultsOverlay(
                    results = it,
                    frameWidth = frameWidth,
                    frameHeight = frameHeight
                )
            }
        }

    }
}

/**
 * the preview is fitted into a container of arbitrary size but preserves aspect ratio.
 * fitting behavior for the preview is calculated for two cases:
 * the box aspect ratio is wider than the container aspect ratio. we need to set
 * the box width to max available width and scale height down to retain aspect ratio
 * of the original box.
 * the box aspect ratio is taller than the container aspect ratio. we need to set
 * box height to max available height and scale down the width to retain aspect ratio
 * of the original box.
 */
fun getFittedBoxSize(containerSize: Size, boxSize: Size): Size {
    val boxAspectRatio = boxSize.width / boxSize.height
    val containerAspectRatio = containerSize.width / containerSize.height

    return if(boxAspectRatio > containerAspectRatio) {
        Size(width = containerSize.width, height = (containerSize.width/boxSize.width) * boxSize.height)
    } else {
        Size(width =  (containerSize.height/boxSize.height) * boxSize.width, height = containerSize.height)
    }
}
