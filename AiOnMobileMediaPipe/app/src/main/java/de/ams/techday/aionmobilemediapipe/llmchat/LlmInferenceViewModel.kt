package de.ams.techday.aionmobilemediapipe.llmchat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.ams.techday.aionmobilemediapipe.llmchat.inference.InferenceModel
import de.ams.techday.aionmobilemediapipe.llmchat.ui.MODEL_PREFIX
import de.ams.techday.aionmobilemediapipe.llmchat.ui.USER_PREFIX
import de.ams.techday.aionmobilemediapipe.llmchat.ui.state.GemmaUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import timber.log.Timber

class LlmInferenceViewModel(
    private val inferenceModel: InferenceModel
) : ViewModel() {

    private val _state = MutableStateFlow(GemmaUiState())
    val state = _state.asStateFlow()

    private val _textInputEnabled = MutableStateFlow(true)
    val textInputEnabled = _textInputEnabled.asStateFlow()

    fun onSendMessage(userMessage: String) {
        val job = viewModelScope.launch(
            context = Dispatchers.IO
        ) {
            _state.value.addMessage(userMessage, USER_PREFIX)
            var currentMessageId: String? = _state.value.createLoadingMessage()
            setInputEnabled(false)
            try {

                val fullPrompt = _state.value.fullPrompt

                inferenceModel.generateResponseAsync(fullPrompt)
                inferenceModel
                    .partialResults
                    .collectIndexed { index: Int, (partialResult, done) ->
                        currentMessageId?.let {
                            if(index == 0) {
                                _state.value.appendMessage(it, partialResult)
                            } else {
                                _state.value.appendMessage(it, partialResult, done)
                            }
                            if(done) {
                                // clean up
                                currentMessageId = null
                                // re-enable text input
                                setInputEnabled(true)
                            }
                        }
                    }
            } catch (ex:Exception) {
                _state.value.addMessage(ex.localizedMessage ?: "unknown error", MODEL_PREFIX)
                setInputEnabled(true)
            }
        }.invokeOnCompletion {
            Timber.d("send message $it complete")
        }
        job.dispose()
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

    companion object {
        fun getFactory(context:Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = InferenceModel.getInstance(context)
                return LlmInferenceViewModel(inferenceModel) as T
            }
        }
    }
}
