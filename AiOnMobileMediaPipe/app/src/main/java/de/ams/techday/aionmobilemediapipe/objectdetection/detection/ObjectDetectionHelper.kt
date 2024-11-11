package de.ams.techday.aionmobilemediapipe.objectdetection.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import timber.log.Timber

class ObjectDetectionHelper(
    var threshold: Float = THRESHOLD_DEFAULT,
    var maxResults: Int = MAX_RESULTS_DEFAULT,
    var currentDelegate: Int = DELEGATE_CPU,
    var currentModel: Int = MODEL_EFFICIENTDETV0,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    var objectDetectionListener: DetectorListener? = null
) {

    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    fun setupObjectDetector() {

        val baseOptionsBuilder = BaseOptions.builder()

        // use specific hardware for running the model. Default is CPU
        when (currentDelegate) {
            DELEGATE_CPU -> baseOptionsBuilder.setDelegate(Delegate.CPU)
            DELEGATE_GPU -> baseOptionsBuilder.setDelegate(Delegate.GPU)
        }

        val modelName = when (currentModel) {
            MODEL_EFFICIENTDETV0 -> "efficientdet-lite0.tflite"
            MODEL_EFFICIENTDETV1 -> "efficientdet-lite1.tflite"
            MODEL_EFFICIENTDETV2 -> "efficientdet-lite2.tflite"
            else -> "efficientdet-lite0.tflite"
        }

        baseOptionsBuilder.setModelAssetPath(modelName)

        // validate that listener for LiveStream is set
        when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                println(" **** RunningMode is $runningMode - do nothing ")
            }

            RunningMode.LIVE_STREAM -> {
                if (objectDetectionListener == null) {
                    throw IllegalStateException("to use LIVE_STREAM the liveStreamDetectorListener must be set")
                }
            }
        }

        try {
            val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setScoreThreshold(threshold)
                .setRunningMode(runningMode)
                .setMaxResults(maxResults)
            when (runningMode) {
                RunningMode.IMAGE,
                RunningMode.VIDEO -> optionsBuilder.setRunningMode(runningMode)

                RunningMode.LIVE_STREAM ->
                    optionsBuilder.setRunningMode(runningMode)
                        .setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLiveStreamError)
            }
            // now build the final options object
            val options = optionsBuilder.build()
            objectDetector = ObjectDetector.createFromOptions(context, options)
        } catch (ex: IllegalStateException) {
            objectDetectionListener?.onError("object detector failed to initialize.")
            Timber.e("tflite model failed to initialize with error: ${ex.message}")
        } catch (ex: RuntimeException) {
            objectDetectionListener?.onError("object detector failed to initialize with ${ex.message}")
            Timber.e("tflite model failed to load with error: ${ex.message}")
        }
    }

    // run object detection on live-stream camera on frame-by-frame basis and returns
    // results asynchronously to caller
    fun detectLiveStreamFrame(imageProxy: ImageProxy) {
        // do not use while in wrong running mode
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException(
                "Attempting to call detectLivestreamFrame" +
                        " while not using RunningMode.LIVE_STREAM"
            )
        }

        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )
        /*
            Accessing Image Planes: When you capture an image using CameraX,
            you can access its planes through the ImageProxy object.
            This object provides access to each plane via methods like getPlanes().
            Usage in Image Processing:
            Each plane can be accessed and manipulated individually, which is useful
            for tasks like image filtering, analysis, or transformations.
         */
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        // close the proxy after usage
        imageProxy.close()
        // rotate the received frame from the camera to conform to display rotation
        val matrix = Matrix().apply {
            postRotate(
                imageProxy.imageInfo.rotationDegrees.toFloat()
            )
        }
        val rotatedBitmap = Bitmap
            .createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                matrix,
                true,
            )

        /*
            mpImage [MediaPipeImage] datastructure used by media pipe vision tasks.
         */
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        /**
         * run inference on the created mpImage
         */
        detectAsync(mpImage, frameTime)
    }

    // Run object detection using Object Detector API
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        // using LIVE_STREAM, results will be returned by
        // returnLivestreamResult function
        objectDetector?.detectAsync(mpImage, frameTime)
    }

    // return results to caller
    private fun returnLivestreamResult(
        result: ObjectDetectionResult,
        input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        objectDetectionListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    // error handling
    private fun returnLiveStreamError(error: RuntimeException) {
        objectDetectionListener?.onError(
            error.message ?: "unknown error encountered"
        )
    }

    // Accepted a Bitmap and runs object detection inference on it to return results back
    // to the caller
    fun detectImage(image: Bitmap): ResultBundle? {

        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException(
                "Attempting to call detectImage" +
                        " while not using RunningMode.IMAGE"
            )
        }

        if (objectDetector == null) return null

        // Inference time is the difference between the system time at the start and finish of the
        // process
        val startTime = SystemClock.uptimeMillis()

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(image).build()

        // Run object detection using MediaPipe Object Detector API
        objectDetector?.detect(mpImage)?.also { detectionResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                listOf(detectionResult),
                inferenceTimeMs,
                image.height,
                image.width
            )
        }

        // If objectDetector?.detect() returns null, this is likely an error. Returning null
        // to indicate this.
        return null
    }

    // Video feed
    // Accepts the URI for a video file loaded from the user's gallery and attempts to run
    // object detection inference on the video. This process will evaluate every frame in
    // the video and attach the results to a bundle that will be returned.
    fun detectVideoFile(
        videoUri: Uri,
        inferenceIntervalMs: Long
    ): ResultBundle? {

        if (runningMode != RunningMode.VIDEO) {
            throw IllegalArgumentException(
                "Attempting to call detectVideoFile" +
                        " while not using RunningMode.VIDEO"
            )
        }

        if (objectDetector == null) return null

        // Inference time is the difference between the system time at the start and finish of the
        // process
        val startTime = SystemClock.uptimeMillis()

        var didErrorOccurred = false

        // Load frames from the video and run the object detection model.
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoLengthMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()

        // Note: We need to read width/height from frame instead of getting the width/height
        // of the video directly because MediaRetriever returns frames that are smaller than the
        // actual dimension of the video file.
        val firstFrame = retriever.getFrameAtTime(0)
        val width = firstFrame?.width
        val height = firstFrame?.height

        // If the video is invalid, returns a null detection result
        if ((videoLengthMs == null) || (width == null) || (height == null)) return null

        // Next, we'll get one frame every frameInterval ms, then run detection on these frames.
        val resultList = mutableListOf<ObjectDetectionResult>()
        val numberOfFrameToRead = videoLengthMs.div(inferenceIntervalMs)

        for (i in 0..numberOfFrameToRead) {
            val timestampMs = i * inferenceIntervalMs // ms

            retriever
                .getFrameAtTime(
                    timestampMs * 1000, // convert from ms to micro-s
                    MediaMetadataRetriever.OPTION_CLOSEST
                )
                ?.let { frame ->
                    // Convert the video frame to ARGB_8888 which is required by the MediaPipe
                    val argb8888Frame =
                        if (frame.config == Bitmap.Config.ARGB_8888) frame
                        else frame.copy(Bitmap.Config.ARGB_8888, false)

                    // Convert the input Bitmap object to an MPImage object to run inference
                    val mpImage = BitmapImageBuilder(argb8888Frame).build()

                    // Run object detection using MediaPipe Object Detector API
                    objectDetector?.detectForVideo(mpImage, timestampMs)
                        ?.let { detectionResult ->
                            resultList.add(detectionResult)
                        }
                        ?: {
                            didErrorOccurred = true
                            objectDetectionListener?.onError(
                                "ResultBundle could not be returned" +
                                        " in detectVideoFile"
                            )
                        }
                }
                ?: run {
                    didErrorOccurred = true
                    objectDetectionListener?.onError(
                        "Frame at specified time could not be" +
                                " retrieved when detecting in video."
                    )
                }
        }

        retriever.release()

        val inferenceTimePerFrameMs =
            (SystemClock.uptimeMillis() - startTime).div(numberOfFrameToRead)

        return if (didErrorOccurred) {
            null
        } else {
            ResultBundle(resultList, inferenceTimePerFrameMs, height, width)
        }
    }

    fun clearObjectDetector() {
        objectDetector?.close()
        objectDetector = null
    }

    companion object {
        // models
        const val MODEL_EFFICIENTDETV0 = 0
        const val MODEL_EFFICIENTDETV2 = 1
        const val MODEL_EFFICIENTDETV1 = 2

        // delegates -> Cpu or GPU
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1

        // model values
        const val MAX_RESULTS_DEFAULT = 3
        const val THRESHOLD_DEFAULT = 0.5F
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }

    // Used to pass results or errors back to the calling class
    interface DetectorListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }

    /**
     * the result from inference.
     * time it took.
     * input image size for UI
     */
    data class ResultBundle(
        val results: List<ObjectDetectionResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )
}
