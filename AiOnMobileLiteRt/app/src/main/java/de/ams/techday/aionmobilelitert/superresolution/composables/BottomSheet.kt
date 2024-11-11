package de.ams.techday.aionmobilelitert.superresolution.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.ams.techday.aionmobilelitert.R
import de.ams.techday.aionmobilelitert.commons.Delegate

@Composable
fun BottomSheet(
    inferenceTime: String,
    modifier: Modifier = Modifier,
    onDelegateSelected: (Delegate) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
        Row {
            Text(
                modifier = Modifier.weight(0.5f),
                text = stringResource(id = R.string.inference_title)
            )
            Text(text = stringResource(id = R.string.inference_value, inferenceTime))
        }

        Spacer(modifier = Modifier.height(10.dp))

        OptionMenu(label = stringResource(id = R.string.delegate_label),
            options = Delegate.entries.map { it.name }) {
            onDelegateSelected(Delegate.valueOf(it))
        }
    }
}

@Composable
fun OptionMenu(
    label: String,
    modifier: Modifier = Modifier,
    options: List<String>,
    onOptionSelected: (option: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var option by remember { mutableStateOf(options.first()) }
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(modifier = Modifier.weight(0.5f), text = label, fontSize = 15.sp)
        Box {
            Row(
                modifier = Modifier.clickable {
                    expanded = true
                }, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = option)
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, contentDescription = ""
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(text = it)
                        },
                        onClick = {
                            option = it
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}