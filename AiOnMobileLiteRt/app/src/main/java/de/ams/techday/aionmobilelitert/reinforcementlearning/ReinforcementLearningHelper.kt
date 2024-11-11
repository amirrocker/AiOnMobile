package de.ams.techday.aionmobilelitert.reinforcementlearning

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import timber.log.Timber
import java.nio.FloatBuffer

class ReinforcementLearningHelper(private val context: Context) {

    /** The TFLite interpreter instance.  */
    private var interpreter: Interpreter? = null

    init {
        initHelper()
    }

    fun initHelper() {
        interpreter = try {
            val litertBuffer = FileUtil.loadMappedFile(
                context,
                "planestrike.tflite"
            )
            Timber.d("loaded model file from assets")
            Interpreter(litertBuffer, Interpreter.Options())
        } catch(e: Exception) {
            Timber.e("Initializing model failed. error: ${e.message}")
            return
        }
    }

    // pass the board and receive a hit?
    suspend fun predict(board:List<List<Int>>):Int {
        return withContext(Dispatchers.IO) {
            assert(interpreter != null) { "Interpreter is null. init interpreter first" }

            val outputShape = interpreter!!.getOutputTensor(0).shape()

            val outputBuffer = FloatBuffer.allocate(outputShape[1])

            val inputArray = arrayOfIntArraysToFloatArrays(board)

            outputBuffer.rewind()

            interpreter!!.run(inputArray, outputBuffer)
            outputBuffer.rewind()

            val output = FloatArray(outputBuffer.capacity())

            outputBuffer.get(output)

            val max = output.max()
            val maxId = output.indexOfFirst {
                it == max
            }
            maxId
        }
    }

    private fun arrayOfIntArraysToFloatArrays(board: List<List<Int>>): Array<FloatArray> {
        return board.map {
            it.map { value ->
                value.toFloat()
            }.toFloatArray()
        }.toTypedArray()
    }
}