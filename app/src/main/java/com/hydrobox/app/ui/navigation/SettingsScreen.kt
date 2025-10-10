package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var dark by remember { mutableStateOf(false) }
    Column(Modifier.padding(16.dp)) {
        Text("Configuración")
        RowWithSwitch("Tema oscuro", dark) { dark = it }
    }
}

@Composable
private fun RowWithSwitch(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row {
        Text(label, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Switch(checked = value, onCheckedChange = onChange)
    }
}
