package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SensorsScreen(paddingValues: PaddingValues) {
    Column(Modifier.padding(paddingValues).padding(16.dp)) {
        Text("Sensores", style = MaterialTheme.typography.headlineSmall)
        Text("Listado de sensores y lecturas (demo)")
    }
}
