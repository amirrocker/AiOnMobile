package de.ams.techday.aionmobilelitert.textclassification

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ams.techday.aionmobilelitert.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextClassificationScreen(
    viewModel: TextClassificationViewModel = viewModel(
        factory = TextClassificationViewModel
            .getFactory(LocalContext.current.applicationContext)
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            Toast.makeText(
                context,
                "${uiState.errorMessage}",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.errorMessageShown()
        }
    }

    BottomSheetScaffold(
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetPeekHeight = 80.dp,
        sheetContent = {
            BottomSheetContent(
                inferenceTime = uiState.inferenceTime,
                onModelSelected = {
                    viewModel.setModel(it)
                },
            )
        }
    ) {
        ClassificationBody(
            positivePercentage = uiState.positivePercentage,
            negativePercentage = uiState.negativePercentage,
            onSubmitted = {
                if (it.isNotBlank()) {
                    viewModel.classifyText(it)
                }
            })
    }
}

@Composable
fun BottomSheetContent(
    inferenceTime: Long,
    modifier: Modifier = Modifier,
    onModelSelected: (Model) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
        Row {
            Text(
                modifier = Modifier.weight(0.5f),
                text = stringResource(id = R.string.inference_title),
                fontSize = 16.sp
            )
            Text(
                text = stringResource(id = R.string.inference_value, inferenceTime),
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        ModelSelection(
            onModelSelected = onModelSelected,
        )
    }
}

@Composable
fun ClassificationBody(
    positivePercentage: Float,
    negativePercentage: Float,
    modifier: Modifier = Modifier,
    onSubmitted: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            value = text,
            onValueChange = {
                text = it
            }, placeholder = {
                Text(text = stringResource(id = R.string.text_field_place_holder))
            })
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmitted(text)
            }) {
            Text(text = stringResource(id = R.string.classify))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.positive, positivePercentage),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.negative, negativePercentage),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModelSelection(
    modifier: Modifier = Modifier,
    onModelSelected: (Model) -> Unit,
) {
    val radioOptions = Model.entries.map { it.name }.toList()
    var selectedOption by remember { mutableStateOf(radioOptions.first()) }

    Column(modifier = modifier) {
        radioOptions.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = {
                        if (selectedOption == option) return@RadioButton
                        onModelSelected(Model.valueOf(option))
                        selectedOption = option
                    }, // Recommended for accessibility with screen readers
                )
                Text(modifier = Modifier.padding(start = 16.dp), text = option, fontSize = 15.sp)
            }
        }
    }
}
