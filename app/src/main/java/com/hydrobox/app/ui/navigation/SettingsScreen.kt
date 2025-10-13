package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hydrobox.app.ui.theme.LocalDarkThemeState

@Composable
fun SettingsScreen() {
    val darkState = LocalDarkThemeState.current
    Column(Modifier.padding(16.dp)) {
        Text("Configuración")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tema oscuro", modifier = Modifier.weight(1f).padding(end = 8.dp))
            Switch(checked = darkState.value, onCheckedChange = { darkState.value = it })
        }
    }
}
