package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.SolidColor

@Composable
fun SensorsScreen(paddingValues: PaddingValues) {
    val items = remember {
        listOf(
            "Temperatura del aire",
            "Humedad del aire",
            "Temperatura del agua",
            "pH del agua",
            "ORP",
            "Nivel del agua"
        )
    }

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Dispositivos Registrados",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        items.forEach { name ->
            DeviceRowPill(
                title = name,
                onDetails = { /* TODO: navegar a detalle del sensor */ }
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DeviceRowPill(
    title: String,
    onDetails: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    // Colores desde tu theme M3 (usa BrandPrimary y SurfaceVariant)
    val neon      = MaterialTheme.colorScheme.primary            // BrandPrimary
    val container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

    Surface(
        color = container,
        shape = shape,
        tonalElevation = 1.dp,
        border = BorderStroke(width = 2.dp, brush = SolidColor(neon)),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .shadow(elevation = 2.dp, shape = shape)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            AssistChip(
                onClick = onDetails,
                label = { Text("Características") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(9999.dp)
            )
        }
    }
}