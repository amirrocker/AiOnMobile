package de.ams.techday.aionmobilelitert.superresolution.sample

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import de.ams.techday.aionmobilelitert.R
import de.ams.techday.aionmobilelitert.commons.Delegate
import java.io.InputStream

@Composable
fun ImageSampleScreen(
    modifier: Modifier = Modifier,
    delegate: Delegate,
    onInferenceTimeChanged: (Int) -> Unit
) {

    val context = LocalContext.current
    val viewModel:ImageSampleViewModel =
        viewModel(factory = ImageSampleViewModel.getFactory(context))

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedBitmap = uiState.selectedBitmap
    val sharpenedBitmap = uiState.sharpenedBitmap

    LaunchedEffect(key1 = uiState.inferenceTime) {
        onInferenceTimeChanged(uiState.inferenceTime)
    }

    LaunchedEffect(key1 = delegate) {
        viewModel.setDelegate(delegate)
    }

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = stringResource(id = R.string.image_sample_choose_image_description))
        Spacer(modifier = Modifier.height(5.dp))

        Row(modifier = modifier) {
            uiState.sampleUriList.forEach {
                AsyncImage(
                    modifier = Modifier
                        .size(70.dp)
                        .clickable {
                            val assetManager = context.assets
                            val inputStream : InputStream = assetManager.open(it)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            viewModel.selectImage(bitmap)
                        },
                    model = "file:///android_asset/$it",
                    contentDescription = null
                )
            }
        }

        Text(
            text = stringResource(id = R.string.image_sample_guide)
        )
        Spacer(modifier = Modifier.height(5.dp))

        Row {
            if (selectedBitmap != null) {
                AsyncImage(
                    modifier = Modifier.size(150.dp),
                    model = selectedBitmap,
                    contentDescription = null,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (sharpenedBitmap != null) {
                AsyncImage(
                    modifier = Modifier.size(150.dp),
                    model = sharpenedBitmap,
                    contentDescription = null,
                )
            }
        }
        if(selectedBitmap != null) {
            Button(
                onClick = {
                    viewModel.sharpenImage()
                }
            ) {
                Text(text = stringResource(R.string.image_sample_up_sample))
            }
        }
    }
}