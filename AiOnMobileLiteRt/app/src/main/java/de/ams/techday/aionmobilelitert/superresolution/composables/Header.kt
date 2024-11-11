package de.ams.techday.aionmobilelitert.superresolution.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.ams.techday.aionmobilelitert.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        title = {
            Image(
                modifier = Modifier.size(120.dp),
                alignment = Alignment.CenterStart,
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
            )
        },
    )
}