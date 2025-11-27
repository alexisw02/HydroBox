package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hydrobox.app.mqtt.HydroMqtt

private enum class DeviceKind { Switchable, Doser }

private data class DeviceRow(
    val name: String,
    val kind: DeviceKind,
    val isOn: MutableState<Boolean> = mutableStateOf(false) // solo para Switchable
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActuatorsScreen(paddingValues: PaddingValues) {
    val devices = remember {
        listOf(
            DeviceRow("Bomba de agua", DeviceKind.Switchable),
            DeviceRow("Ventilación", DeviceKind.Switchable),
            DeviceRow("Luz de cultivo", DeviceKind.Switchable),
            DeviceRow("Peristáltica A — FloraMicro", DeviceKind.Doser),
            DeviceRow("Peristáltica B — FloraGrow", DeviceKind.Doser),
            DeviceRow("Peristáltica C — FloraBloom", DeviceKind.Doser),
        )
    }

    var dosingDevice by remember { mutableStateOf<DeviceRow?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Actuadores Registrados",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        )

        devices.forEach { row ->
            DeviceRowPill(
                title = row.name,
                kind = row.kind,
                isOn = row.isOn,
                onToggle = { new ->
                    row.isOn.value = new
                    val id = deviceIdFor(row.name)
                    HydroMqtt.sendSwitch(id, new)
                },
                onDoseClick = { dosingDevice = row },
                onDetails = {
                    // TODO: navegar a “Características” de este actuador si lo deseas
                    // navController.navigate("actuator/${row.name}")
                }
            )
        }

        Spacer(Modifier.weight(1f))
    }

    // Sheet de dosificación para peristálticas
    if (dosingDevice != null) {
        ModalBottomSheet(
            onDismissRequest = { dosingDevice = null },
            sheetState = sheetState
        ) {
            DoseSheet(
                deviceName = dosingDevice!!.name,
                onStartAuto = {
                    dosingDevice = null
                },
                onStartManual = { ml ->
                    val id = deviceIdFor(dosingDevice!!.name)
                    HydroMqtt.sendDose(id, ml)
                    dosingDevice = null
                },
                onClose = { dosingDevice = null }
            )
        }
    }
}
private fun deviceIdFor(title: String): String =
    when {
        title.startsWith("Bomba", ignoreCase = true)        -> "pump"
        title.startsWith("Ventilación", ignoreCase = true)  -> "fan"
        title.startsWith("Luz", ignoreCase = true)          -> "light"
        title.contains("Micro", ignoreCase = true)          -> "doser_a"
        title.contains("Grow", ignoreCase = true)           -> "doser_b"
        title.contains("Bloom", ignoreCase = true)          -> "doser_c"
        else                                                -> title.lowercase().replace(" ", "_")
    }

@Composable
private fun DeviceRowPill(
    title: String,
    kind: DeviceKind,
    isOn: State<Boolean>,
    onToggle: (Boolean) -> Unit,
    onDoseClick: () -> Unit,
    onDetails: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val cs    = MaterialTheme.colorScheme
    val neon  = cs.primary
    val bg    = pillBackgroundBrush(cs)

    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        color = Color.Transparent,
        shape = shape,
        tonalElevation = 1.dp,
        border = BorderStroke(2.dp, SolidColor(neon)),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .shadow(elevation = 2.dp, shape = shape)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(bg, shape)
                .border(BorderStroke(1.dp, SolidColor(cs.onSurface.copy(alpha = 0.06f))), shape)
                .padding(horizontal = 18.dp)
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (kind == DeviceKind.Switchable) {
                    AssistChip(
                        onClick = { /* indicador */ },
                        label = { Text(if (isOn.value) "Activo" else "Apagado") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isOn.value) cs.primaryContainer else cs.surfaceVariant,
                            labelColor = if (isOn.value) cs.onPrimaryContainer else cs.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(9999.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Box {
                    FilledTonalIconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        when (kind) {
                            DeviceKind.Switchable -> {
                                val next = !isOn.value
                                DropdownMenuItem(
                                    text = { Text(if (next) "Activar" else "Apagar") },
                                    onClick = { menuOpen = false; onToggle(next) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Características") },
                                    onClick = { menuOpen = false; onDetails() }
                                )
                            }
                            DeviceKind.Doser -> {
                                DropdownMenuItem(
                                    text = { Text("Dosificar…") },
                                    onClick = { menuOpen = false; onDoseClick() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Características") },
                                    onClick = { menuOpen = false; onDetails() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun pillBackgroundBrush(cs: ColorScheme): Brush {
    val isDark = cs.background.luminance() < 0.5f
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                cs.primary.copy(alpha = 0.24f),
                cs.primaryContainer.copy(alpha = 0.40f),
                cs.surfaceVariant.copy(alpha = 0.18f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                cs.primaryContainer.copy(alpha = 0.70f),
                cs.primary.copy(alpha = 0.30f),
                cs.surface.copy(alpha = 0.55f)
            )
        )
    }
}

@Composable
private fun DoseSheet(
    deviceName: String,
    onStartAuto: () -> Unit,
    onStartManual: (Int) -> Unit,
    onClose: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var mlText by remember { mutableStateOf("10") }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Dosificar — $deviceName",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tab == 0,
                onClick = { tab = 0 },
                label = { Text("Automática") }
            )
            FilterChip(
                selected = tab == 1,
                onClick = { tab = 1 },
                label = { Text("Manual") }
            )
        }

        when (tab) {
            0 -> {
                Text(
                    "Aplica la dosis según el perfil guardado (ej. 5 ml cada 4 h).",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedButton(onClick = onStartAuto) { Text("Iniciar automática") }
                    OutlinedButton(onClick = onClose) { Text("Cancelar") }
                }
            }
            1 -> {
                OutlinedTextField(
                    value = mlText,
                    onValueChange = { if (it.length <= 4) mlText = it.filter { c -> c.isDigit() } },
                    label = { Text("Mililitros (ml)") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedButton(onClick = {
                        val ml = mlText.toIntOrNull() ?: 0
                        if (ml > 0) onStartManual(ml)
                    }) { Text("Dosificar ahora") }
                    OutlinedButton(onClick = onClose) { Text("Cancelar") }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

