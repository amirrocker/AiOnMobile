package de.ams.techday.aionmobilemediapipe.llmchat.inference

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import javax.inject.Inject

class InferenceModel @Inject constructor(context: Context) {

    /* this is where the magic happens */
    private var llmInference: LlmInference

    private val _partialResults: MutableSharedFlow<Pair<String, Boolean>> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    private val modelExists: Boolean
        get() = File(MODEL_PATH).exists()

    init {

        if(modelExists.not()) {
            throw IllegalStateException("model does not exist")
        }

        val options = LlmInference.LlmInferenceOptions.builder()

            /**
             * self explanatory. Where do we find the model. Currently a
             *
             */
            .setModelPath(MODEL_PATH)

            /**
             *
             * In MediaPipe’s LlmInference API, the setTemperature() method is used to control the
             * randomness of the model’s output during language generation, a technique commonly
             * applied in Large Language Models (LLMs). The "temperature" setting affects
             * how deterministic or diverse the generated output will be,
             * balancing between creativity and predictability.
             *
             * Method Description
             * The setTemperature() method adjusts the model's sampling temperature, influencing
             * how likely the model is to produce varied responses. Lowering the temperature makes
             * the output more focused and deterministic, while increasing it introduces greater
             * randomness and diversity in the output.
             *
             * Parameters
             * temperature (float): The sole parameter for setTemperature(), it adjusts
             * the level of randomness in the model's responses.
             *
             * Accepted Values and Effects
             * The value range for temperature is generally from 0.0 to around 2.0, with 1.0
             * being a typical default. Here’s what different values do:
             * 0.0: Produces the most deterministic output, as it forces the model to pick the
             * highest-probability next token. This can lead to repetitive or
             * overly predictable text.
             *
             * 0.1 to 0.5: Keeps responses fairly focused and predictable, with low randomness.
             * 1.0: Represents "balanced" randomness, providing diverse responses without
             * too much unpredictability.
             *
             * Above 1.0: Increases randomness, leading to more creative or unexpected responses,
             * which may occasionally be less relevant or coherent.
             * 2.0 and above: Produces highly varied and unpredictable responses,
             * often sacrificing coherence for creativity.
             *
             * TODO change this setting to make the model behave different
             */
            .setTemperature(.75f)

            /**
             * In MediaPipe’s LlmInference API, the setTopK() method is used to limit the number of
             * possible next tokens that the model considers at each step during text generation.
             * This technique, known as Top-K Sampling, controls how many of the most likely
             * next tokens the model will consider, effectively narrowing or broadening
             * the range of potential outputs.
             *
             * Method Description
             * The setTopK() method restricts the model to sampling only from the top K tokens
             * with the highest probabilities, discarding all tokens that fall outside
             * this top K group. This can help balance response diversity and relevance
             * by focusing only on the most likely next steps.
             *
             * Parameters
             * topK (int): The sole parameter for setTopK(), representing the number of tokens
             * to include in the sampling pool.
             *
             * Accepted Values and Effects
             * The value range for topK is typically from 1 to a large integer (e.g., 50 or more),
             * depending on how diverse you want the output to be:
             *
             * 1: Completely deterministic output, as it will always choose the single most likely
             * next token (effectively disabling randomness).
             *
             * 5 to 10: Narrows the model’s choices, producing output
             * that is fairly consistent and predictable.
             *
             * 20 to 40: Balances diversity and relevance, often providing
             * coherent and interesting responses.
             *
             * Higher values (e.g., 50+): Allows for a larger pool of possible next tokens,
             * increasing response variability and creativity
             * but possibly reducing relevance or coherence.
             *
             * 0 or -1: May be interpreted by some implementations to consider
             * all tokens (no restriction), providing maximal diversity
             * but potentially less coherent results.
             *
             * TODO change this setting to make the model behave different
             */
            .setTopK(30)

            /**
             * In MediaPipe’s LlmInference API, the setRandomSeed() method sets a fixed seed
             * for the random number generator used in the model’s text generation process.
             * This is valuable for making model outputs reproducible, as setting
             * the same seed allows the model to produce the same output
             * given the same input across different runs.
             *
             * Method Description
             * The setRandomSeed() method fixes the randomness of the generation process,
             * making results consistent. This is particularly useful for testing and debugging,
             * as it allows you to get predictable outputs and analyze
             * the model’s behavior under controlled conditions.
             *
             * Parameters
             * seed (long): The sole parameter for setRandomSeed(),
             * which specifies the random seed value.
             *
             * Accepted Values and Effects
             * The value range for seed is any valid integer (positive or negative)
             * within the range of a long data type:
             *
             * Fixed integer values (e.g., 42, 1234): Using a specific value (e.g., 42)
             * ensures deterministic results, meaning the output remains the same
             * across runs if other generation settings and inputs are also the same.
             *
             * Random or changing values: Leaving the seed unset or passing in different
             * seed values each time allows for varied outputs,
             * as the generation process will introduce new randomness.
             *
             * TODO change this setting to make the model behave different
             */
//            .setRandomSeed()

            /**
             * In MediaPipe’s LlmInference API, the setMaxTokens() method controls
             * the maximum length of the generated output in terms of tokens.
             * Tokens can be individual words, subwords, or characters,
             * depending on the language model used.
             * This setting is useful for managing the verbosity of the response
             * and preventing overly lengthy outputs.
             *
             * Method Description
             * The setMaxTokens() method sets an upper limit on the number of tokens the model
             * can generate in a single inference. Once this token limit is reached,
             * the model stops generating further tokens. This is particularly useful
             * when you want to constrain the response length,
             * either for efficiency or to ensure concise answers.
             *
             * Parameters
             * maxTokens (int): The sole parameter for setMaxTokens(), which represents
             * the maximum number of tokens the model can generate.
             *
             * Accepted Values and Effects
             * The value range for maxTokens is typically set between 1 and a higher integer
             * based on your output length requirements and model capabilities:
             *
             * 1 to 10: Limits output to very brief responses,
             * typically single words or short phrases.
             *
             * 20 to 50: Produces concise answers, such as single sentences or short paragraphs.
             *
             * 100 to 200: Allows for more detailed responses, like longer paragraphs
             * or even short summaries.
             *
             * 500+: Generates long-form content, which may include multiple paragraphs,
             * depending on the prompt and the model’s design.
             *
             * TODO change this setting to make the model behave different
             */
            .setMaxTokens(1024)

            .setResultListener { partialResult, done ->
                _partialResults.tryEmit(partialResult to done)
            }
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun generateResponseAsync(prompt: String) {
        val gemmaPrompt = "$prompt<start_of_turn>model\n"
        llmInference.generateResponseAsync(gemmaPrompt)
    }

    companion object {

        // use unique model names because weight caching is currently based on filename alone.
        private const val MODEL_PATH = "/data/local/tmp/llm/gemma-2b-it-gpu-int4.bin"
        private var instance: InferenceModel? = null

        fun getInstance(context: Context): InferenceModel {
            return if (instance != null) {
                instance!!
            } else {
                InferenceModel(context).also { instance = it }
            }
        }
    }
}