package de.ams.techday.aionmobilelitert.textclassification

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TextClassificationViewModel(
    private val textClassificationHelper: TextClassificationHelper
) : ViewModel() {

    private var classifyJob: Job? = null

    init {
        viewModelScope.launch {
            textClassificationHelper.initInterpreter()
        }
    }

    private val percentages = textClassificationHelper.percentages.distinctUntilChanged().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        Pair(
            FloatArray(0),
            0L
        )
    )

    private val errorMessage = MutableStateFlow<Throwable?>(null).also {
        viewModelScope.launch {
            textClassificationHelper.error.collect(it)
        }
    }

    val uiState: StateFlow<UiState> = combine(
        percentages,
        errorMessage
    ) { percentages, throwable ->
        textClassificationHelper.completableDeferred?.complete(Unit)
        val percentage = percentages.first
        UiState(
            negativePercentage = if(percentage.isNotEmpty()) percentage.first() else 0f,

            positivePercentage = if(percentage.isNotEmpty()) percentage.last() else 0f,

            inferenceTime = percentages.second,
            errorMessage = throwable?.message
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState()
    )

    /**
     * cancel any running job and rerun to
     * classify in new coroutine
     */
    fun classifyText(inputText: String) {
        classifyJob?.cancel()
        classifyJob = viewModelScope.launch {
            textClassificationHelper.classify(inputText)
        }
    }

    fun setModel(model: Model) {
        viewModelScope.launch {
            textClassificationHelper.stopClassify()
            textClassificationHelper.initInterpreter(model)
        }
    }

    /** Clear error message after it has been consumed*/
    fun errorMessageShown() {
        errorMessage.update { null }
    }

    companion object {
        fun getFactory(context: Context) = object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val textClassificationHelper = TextClassificationHelper(context)
                return TextClassificationViewModel(textClassificationHelper) as T
            }
        }
    }
}