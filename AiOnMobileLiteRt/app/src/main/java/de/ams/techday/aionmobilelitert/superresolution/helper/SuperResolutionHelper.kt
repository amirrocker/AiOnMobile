package de.ams.techday.aionmobilelitert.superresolution.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import de.ams.techday.aionmobilelitert.commons.Delegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import timber.log.Timber
import java.nio.FloatBuffer

data class Result(
    val bitmap: Bitmap? = null,
    val inferenceTime: Long = 0L
)

class SuperResolutionHelper(private val context: Context) {

    private var interpreter: Interpreter? = null

    private val _superResolutionFlow = MutableSharedFlow<Result>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val superResolutionFlow: SharedFlow<Result> = _superResolutionFlow.asSharedFlow()

    init {
        initInterpreter()
    }

    fun initInterpreter(delegate: Delegate = Delegate.CPU) {
        interpreter = try {
            val litertBuffer = FileUtil.loadMappedFile(context, "ESRGAN.tflite")
            Timber.d("initialized litert buffer from model")
            val options = Interpreter.Options().apply {
                numThreads = 4
                useNNAPI = delegate == Delegate.CPU
            }
            Interpreter(litertBuffer, options)
        } catch (ex: Exception) {
            Timber.e("Initializing model failed. error: ${ex.message}")
            return
        }
    }

    suspend fun makeSuperResolution(bitmap: Bitmap) {
        require(interpreter != null) { "interpreter must not be null" }
        withContext(Dispatchers.IO) {
            val startTime = SystemClock.uptimeMillis()
            // either we get a shape [_, height, width, _] or we bail
            val (_, h, w, _) = interpreter?.getInputTensor(0)?.shape() ?: return@withContext
            val imageProcessor = ImageProcessor
                .Builder()
                // BILINEAR provides smoother, higher-quality results but at a slightly higher computational cost.
                // NEAREST_NEIGHBOR is faster but may result in a pixelated look, especially when scaling up images.
                .add(ResizeOp(h, w, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 1f))
                .build()
            // preprocess the image and convert to TensorImage for super resolution
            val tensorImage = imageProcessor.process(
                TensorImage.fromBitmap(bitmap)
            )
            // process the tensorImage just resized and normalized
            val outputBitmap = process(tensorImage)
            val inferenceTime =  SystemClock.uptimeMillis() - startTime
            // update state
            _superResolutionFlow.emit(
                Result(
                    bitmap = outputBitmap,
                    inferenceTime = inferenceTime
                )
            )
        }
    }

    private fun process(tensorImage: TensorImage):Bitmap {
        val (_, w, h, c) = interpreter!!.getOutputTensor(0).shape()
        // allocate memory of size width * height * color channels for output
        val outputBuffer = FloatBuffer.allocate(h * w * c)
        // allocate a buffer from image size for input
        val inputBuffer = TensorImage.createFrom(tensorImage, DataType.FLOAT32).buffer
        // instruct interpreter to do its thing - in this case its super resolution
        interpreter?.run(inputBuffer, outputBuffer)
        return floatArrayToBitmap(outputBuffer.array(), w, h)
    }

    private fun floatArrayToBitmap(floatArray: FloatArray, width: Int, height: Int):Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)

        val minVal = floatArray.minOrNull() ?: 0f
        val maxVal = floatArray.maxOrNull() ?: 0f
        val normalizedArray = floatArray.map {
            (it - minVal) / (maxVal - minVal)
                // fancy way of writing 0.00001
                .coerceAtLeast(1.0E-5F)
        }
        // convert the normalized array back to pixel format
        for (i in pixels.indices) {
            val r = (normalizedArray[i * 3] * 255).toInt()
            val g = (normalizedArray[i * 3 + 1] * 255).toInt()
            val b = (normalizedArray[i * 3 + 2] * 255).toInt()
            pixels[i] = Color.argb(255, r, g, b)
        }
        /**
         * http:// developer. android. com/ reference/ android/ graphics/ Bitmap
         * offset: set to zero
         * stride: set to width
         * x, y: set to top left corner
         * width, height: well, you know
         */
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

}

