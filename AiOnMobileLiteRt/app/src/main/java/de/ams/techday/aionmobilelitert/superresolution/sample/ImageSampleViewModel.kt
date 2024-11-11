package de.ams.techday.aionmobilelitert.superresolution.sample

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.ams.techday.aionmobilelitert.commons.Delegate
import de.ams.techday.aionmobilelitert.superresolution.helper.Result
import de.ams.techday.aionmobilelitert.superresolution.helper.SuperResolutionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageSampleViewModel(
    private val superResolutionHelper: SuperResolutionHelper
) : ViewModel() {

    private val selectedBitmapFlow = MutableStateFlow<Bitmap?>(null)
    private val superResolutionFlow = MutableStateFlow(Result())
    private var sharpenJob: Job? = null

    val uiState: StateFlow<ImageSampleUiState> = combine(
        selectedBitmapFlow,
        superResolutionFlow
    ) { selectedBitmap, result ->
        ImageSampleUiState(
            selectedBitmap = selectedBitmap,
            sharpenedBitmap = result.bitmap,
            inferenceTime = result.inferenceTime.toInt()
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ImageSampleUiState())

    init {
        viewModelScope.launch {
            superResolutionHelper.superResolutionFlow
                .collect(superResolutionFlow)
        }
    }

    fun selectImage(bitmap:Bitmap) {
        // clear any prev. results
        superResolutionFlow.update { Result() }
        selectedBitmapFlow.update {
            // garbage collect the prev. selected bitmap
            it?.recycle()
            // now set the selected bitmap
            bitmap
        }
    }

    fun sharpenImage() {
        if(sharpenJob?.isCompleted == false) return

        val bitmap = selectedBitmapFlow.value ?: return
        sharpenJob = viewModelScope.launch(Dispatchers.IO) {
            if(bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap.setConfig(Bitmap.Config.ARGB_8888)
            }
            superResolutionHelper.makeSuperResolution(bitmap)
        }
    }
    /**
     * Set [ImageSuperResolutionHelper.Delegate] (CPU/NNAPI)
     * for ImageSuperResolutionHelper
     *
     */
    fun setDelegate(delegate: Delegate) {
        superResolutionHelper.initInterpreter(delegate)
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val superResolutionHelper = SuperResolutionHelper(context)
                return ImageSampleViewModel(superResolutionHelper) as T
            }
        }
    }

}


