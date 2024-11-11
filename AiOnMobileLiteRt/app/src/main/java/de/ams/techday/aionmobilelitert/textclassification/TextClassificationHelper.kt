package de.ams.techday.aionmobilelitert.textclassification

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.metadata.MetadataExtractor
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

enum class Model(val fileName: String) {
    MobileBERT("mobile_bert.tflite"),
    AverageWordVec("word_vec.tflite")
}

class TextClassificationHelper(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var vocabularyMap = mutableMapOf<String, Int>()

    /**
     * A Deferred that can be completed via public functions complete or cancel.
     * Note that the complete function returns false when this deferred value is already
     * complete or completing, while cancel returns true as long as the deferred is
     * still cancelling and the corresponding exception is incorporated
     * into the final completion exception.
     * An instance of completable deferred can be created by CompletableDeferred() function
     * in active state.
     * All functions on this interface are thread-safe and can be safely invoked
     * from concurrent coroutines without external synchronization.
     **/
    var completableDeferred: CompletableDeferred<Unit>? = null

    /* handle error states */
    private val _error = MutableSharedFlow<Throwable?>()
    val error: SharedFlow<Throwable?> = _error.asSharedFlow()

    /** As the result of text classification, this value emits map of probabilities */
    private val _percentages = MutableSharedFlow<Pair<FloatArray, Long>>(
        extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val percentages: SharedFlow<Pair<FloatArray, Long>> = _percentages.asSharedFlow()

    init {
        initInterpreter()
    }

    fun initInterpreter(model: Model = Model.MobileBERT) {
        interpreter = try {
            val litertBuffer = FileUtil.loadMappedFile(context, model.fileName)
            loadModelMetadata(litertBuffer)
            Interpreter(litertBuffer, Interpreter.Options())
        } catch(e: Exception) {
            Timber.e("Initializing interpreter failed with ${e.message}")
            return
        }
    }

    private fun loadModelMetadata(litertBuffer: ByteBuffer) {
        val metadataExtractor = MetadataExtractor(litertBuffer)
        if (metadataExtractor.hasMetadata()) {
            val vocalBuffer = metadataExtractor.getAssociatedFile("vocab.txt")
            vocabularyMap.putAll(getVocabulary(vocalBuffer))
            Timber.i("Successfully loaded model metadata")
        }
    }

    /** Retrieve vocabularies from "vocab.txt" file metadata*/
    private fun getVocabulary(inputStream: InputStream): Map<String, Int> {
        val reader = BufferedReader(InputStreamReader(inputStream))

        val map = mutableMapOf<String, Int>()
        var index = 0
        var line = ""
        while (reader.readLine().also { if (it != null) line = it } != null) {
            map[line] = index
            index++
        }

        reader.close()
        Timber.d("loadVocabulary: ${map.size}")
        return map
    }

    suspend fun classify(inputText: String) {
        withContext(Dispatchers.IO) {
            if (interpreter == null) return@withContext
            val inputShape = interpreter!!.getInputTensor(0)?.shape() ?: return@withContext
            val outputShape = interpreter!!.getOutputTensor(0)?.shape() ?: return@withContext

            val inputBuffer = IntBuffer.allocate(inputShape[1])
            val outputBuffer = FloatBuffer.allocate(outputShape[1])

            val tokenizerText = tokenizeText(inputText)
            if (tokenizerText.size > inputShape[1]) {
                Timber.e("The number of word exceeds the limit")
                _error.emit(Throwable("The number of word exceeds the limit, please input the number of word <= ${inputShape[1]}"))
                return@withContext
            }
            completableDeferred?.await()
            inputBuffer.put(tokenizerText.toIntArray())
            completableDeferred = CompletableDeferred()

            inputBuffer.rewind()
            outputBuffer.rewind()

            val startTime = SystemClock.uptimeMillis()
            interpreter!!.run(inputBuffer, outputBuffer)
            val inferenceTime = SystemClock.uptimeMillis() - startTime

            val output = FloatArray(outputBuffer.capacity())
            outputBuffer.rewind()
            outputBuffer.get(output)

            /*
             * MobileBERT labels: negative & positive
             * AverageWordVec: 0 & 1
             */
            _percentages.tryEmit(Pair(output, inferenceTime))
        }
    }

    /** Tokenize the text from String to int[], based on the index of words in `voca.txt` */
    private fun tokenizeText(inputText: String): List<Int> {
        return try {
            val nonePunctuationText = removePunctuation(inputText)
            val result = nonePunctuationText.split(" ")
            val ids = mutableListOf<Int>()
            result.forEach { text ->
                if (vocabularyMap.containsKey(text)) {
                    ids.add(vocabularyMap[text]!!)
                } else {
                    ids.add(0)
                }
            }
            Timber.i("tokenizeText: $ids")
            return ids
        } catch (e: IOException) {
            Timber.e("Failed to read vocabulary.txt: ${e.message}")
            emptyList()
        }
    }

    /** Remove punctuation to reduce unnecessary inputs*/
    private fun removePunctuation(text: String): String {
        return text.replace("[^a-zA-Z0-9 ]".toRegex(), "")
    }

    /** Stop current Interpreter*/
    fun stopClassify() {
        if (interpreter != null) {
            interpreter!!.close()
            interpreter = null
        }
    }
}