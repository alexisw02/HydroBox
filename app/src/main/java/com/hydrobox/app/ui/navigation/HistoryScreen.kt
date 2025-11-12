package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hydrobox.app.ui.components.HydroCard
import com.hydrobox.app.ui.components.SegmentedChip
import com.hydrobox.app.ui.components.SegmentedVariant
import com.hydrobox.app.ui.theme.BrandPrimary
import com.hydrobox.app.ui.theme.Error
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(paddingValues: PaddingValues) {
    var range by remember { mutableStateOf(TimeRange.Last7d) }
    var metric by remember { mutableStateOf(Metric.PH) }

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Historial",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Tendencias y eventos del cultivo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Rango
        SectionTitle("Rango")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(TimeRange.values()) { item ->
                SegmentedChip(
                    text = item.label,
                    selected = item == range,
                    onClick = { range = item },
                    variant = SegmentedVariant.Primary
                )
            }
        }

        // Métrica
        SectionTitle("Métrica")
        FlowRow(
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Metric.values().forEach { m ->
                SegmentedChip(
                    text = m.label,
                    selected = m == metric,
                    onClick = { metric = m },
                    variant = SegmentedVariant.Tonal
                )
            }
        }

        // Tendencia
        HydroCard(
            title = "Tendencia",
            subtitle = "${metric.label} — ${range.label}",
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                TrendPlaceholder()
            }
        }

        // Eventos
        HydroCard(
            title = "Eventos",
            subtitle = "Alarmas, cambios de actuadores y calibraciones",
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HistoryEventRow(
                    title = "Bomba de agua — ON",
                    subtitle = "Programado | 10:00",
                    accent = BrandPrimary
                )
                HistoryEventRow(
                    title = "pH fuera de rango",
                    subtitle = "Lectura = 5.5 | Óptimo: 5.8–6.2",
                    accent = Error
                )
                HistoryEventRow(
                    title = "Extractor — OFF",
                    subtitle = "Manual | 12:43",
                    accent = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HistoryEventRow(
                    title = "Dosificación Micro — 8 ml",
                    subtitle = "Rutina semanal | 10:02",
                    accent = BrandPrimary
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun HistoryEventRow(
    title: String,
    subtitle: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // barra de acento a la izquierda
        Box(
            Modifier
                .width(6.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent)
        )
        Column(Modifier.padding(start = 12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun TrendPlaceholder() {
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    val lineColor = BrandPrimary

    Canvas(Modifier.fillMaxSize().padding(12.dp)) {
        val stepY = size.height / 4f
        val stepX = size.width / 6f
        // Rejilla
        for (i in 1..3) {
            val y = stepY * i
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        for (j in 1..5) {
            val x = stepX * j
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        }
        // Línea simple
        val points = listOf(
            Offset(0f, size.height * 0.65f),
            Offset(size.width * 0.18f, size.height * 0.35f),
            Offset(size.width * 0.36f, size.height * 0.55f),
            Offset(size.width * 0.54f, size.height * 0.30f),
            Offset(size.width * 0.72f, size.height * 0.50f),
            Offset(size.width * 0.90f, size.height * 0.28f),
            Offset(size.width, size.height * 0.40f),
        )
        for (k in 0 until points.lastIndex) {
            drawLine(
                color = lineColor,
                start = points[k],
                end = points[k + 1],
                strokeWidth = 4f
            )
        }
    }
}

private enum class TimeRange(val label: String) {
    Today("Hoy"), Last7d("7 días"), Last30d("30 días"), Last90d("90 días"), All("Todo")
}

private enum class Metric(val label: String) {
    PH("pH"), ORP("ORP"), WaterTemp("Temp Agua"),
    AirTemp("Temp Aire"), Humidity("Humedad"), Level("Nivel Agua")
}
