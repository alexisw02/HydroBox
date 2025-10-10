package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActuatorsScreen(paddingValues: PaddingValues) {
    Column(Modifier.padding(paddingValues).padding(16.dp)) {
        Text("Actuadores", style = MaterialTheme.typography.headlineSmall)
        Text("Listado de actuadores a utilizar (demo)")
    }
}
