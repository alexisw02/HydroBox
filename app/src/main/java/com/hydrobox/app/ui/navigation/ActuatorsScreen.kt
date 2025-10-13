package com.hydrobox.app.ui.navigation

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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActuatorsScreen(paddingValues: PaddingValues) {
    val items = remember {
        listOf(
            "Bomba de agua",
            "Ventilación",
            "Luz de cultivo",
            "Peristáltica A — FloraMicro",
            "Peristáltica B — FloraGrow",
            "Peristáltica C — FloraBloom"
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
                onDetails = {}
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
    val neon = MaterialTheme.colorScheme.primary

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (MaterialTheme.colorScheme.isLight()) 0.35f else 0.20f),
            MaterialTheme.colorScheme.primary.copy(alpha = if (MaterialTheme.colorScheme.isLight()) 0.10f else 0.08f)
        )
    )

    Surface(
        color = Color.Transparent,          // lo pintamos con el Box interno
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
                .background(bgBrush, shape)
                .border(
                    BorderStroke(1.dp, SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))),
                    shape
                )
                .padding(horizontal = 18.dp),
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
}

@Composable
private fun androidx.compose.material3.ColorScheme.isLight(): Boolean =
    this.onBackground.alpha < 0.9f