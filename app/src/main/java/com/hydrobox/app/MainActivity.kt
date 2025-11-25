package com.hydrobox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.hydrobox.app.mqtt.HydroMqtt
import com.hydrobox.app.ui.navigation.HydroNavRoot
import com.hydrobox.app.ui.theme.HydroBoxTheme
import com.hydrobox.app.ui.theme.LocalDarkThemeState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === MQTT: configura y conecta ===
        // Cambia por la IP/host de tu Raspberry Pi (o dominio público si aplica)
        HydroMqtt.host = "192.168.3.201"
        // Opcional: si cambiaste el puerto/credenciales en el broker:
        HydroMqtt.port = 1883
        HydroMqtt.user = "hydrobox"
        HydroMqtt.pass = "fokinpipol123"
        HydroMqtt.connect()

        setContent {
            val darkState = rememberSaveable { mutableStateOf(true) }

            CompositionLocalProvider(LocalDarkThemeState provides darkState) {
                HydroBoxTheme(
                    darkTheme = darkState.value,
                    dynamicColor = false
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HydroNavRoot()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        // === MQTT: desconecta limpio ===
        HydroMqtt.disconnect()
        super.onDestroy()
    }
}

