package de.ams.techday.aionmobilemediapipe.objectdetection.detection

class ObjectDetectorListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (resultBundle: ObjectDetectionHelper.ResultBundle) -> Unit
) : ObjectDetectionHelper.DetectorListener {

    override fun onError(error: String, errorCode: Int) {
        onErrorCallback(error, errorCode)
    }

    override fun onResults(resultBundle: ObjectDetectionHelper.ResultBundle) {
        onResultsCallback(resultBundle)
    }
}