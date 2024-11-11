package de.ams.techday.aionmobilelitert.superresolution

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.ams.techday.aionmobilelitert.commons.Delegate
import de.ams.techday.aionmobilelitert.superresolution.composables.BottomSheet
import de.ams.techday.aionmobilelitert.superresolution.composables.Header
import de.ams.techday.aionmobilelitert.superresolution.gallery.ImagePickerScreen
import de.ams.techday.aionmobilelitert.superresolution.sample.ImageSampleScreen

enum class MenuTab {
    ImageSample, Gallery,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperResolutionScreen() {

    var tabState by remember { mutableStateOf(MenuTab.ImageSample) }
    var inferenceTimeState by remember { mutableStateOf("--") }
    var delegateState by remember { mutableStateOf(Delegate.CPU) }

    BottomSheetScaffold(
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetPeekHeight = 80.dp,
        sheetContent = {
            BottomSheet(
                inferenceTime = inferenceTimeState,
                onDelegateSelected = {
                    delegateState = it
                }
            )
        }
    ) {
        Column {
            Header()
            Content(
                tab = tabState,
                delegate = delegateState,
                onTabChanged = { tabState = it },
                onInferenceTimeChanged = { inferenceTimeState = it.toString() }
            )
        }
    }
}

@Composable
fun Content(
    tab: MenuTab,
    delegate: Delegate,
    modifier: Modifier = Modifier,
    onInferenceTimeChanged: (Int) -> Unit,
    onTabChanged: (MenuTab) -> Unit
) {

    val tabs = MenuTab.entries
    Column(modifier) {
        TabRow(
            containerColor = MaterialTheme.colorScheme.primary,
            selectedTabIndex = tab.ordinal
        ) {
            tabs.forEach { currentTab ->
                Tab(
                    text = { Text(text = currentTab.name, color = MaterialTheme.colorScheme.onPrimary) },
                    selected = tab == currentTab,
                    onClick = { onTabChanged(currentTab) }
                )
            }
        }

        when(tab) {
            MenuTab.ImageSample -> ImageSampleScreen(
                delegate = delegate,
                onInferenceTimeChanged = onInferenceTimeChanged
            )
            MenuTab.Gallery -> ImagePickerScreen(
                delegate = delegate,
                onInferenceTimeChanged = onInferenceTimeChanged
            )
        }
    }
}
