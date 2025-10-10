package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hydrobox.app.ui.model.Crop
import kotlin.math.roundToInt

data class DayStep(val label: String)
data class SensorCardData(
    val title: String,
    val valueText: String,
    val subtitle: String,
    val icon: @Composable () -> Unit,
    val percent: Float? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeScreen(paddingValues: PaddingValues) {
    val totalDays = 24
    var currentDay by remember { mutableIntStateOf(12) }

    val currentCrop = remember { Crop.LECHUGA }

    val daysUi = remember {
        listOf("17","18","19","20","21","22","23").map { DayStep(it) }
    }

    val sensors = remember {
        listOf(
            SensorCardData(
                title = "Temperatura del aire",
                valueText = "24.1 °C",
                subtitle = "24° – 27°  |  26/2 21:23:04",
                icon = { Icon(Icons.Filled.DeviceThermostat, contentDescription = null) },
                percent = 0.65f
            ),
            SensorCardData(
                title = "Humedad del aire",
                valueText = "46.7 %",
                subtitle = "Rango óptimo 45–60 %",
                icon = { Icon(Icons.Filled.Opacity, contentDescription = null) },
                percent = 0.47f
            ),
            SensorCardData(
                title = "pH del agua",
                valueText = "6.5",
                subtitle = "Ideal 5.8 – 6.5",
                icon = { Icon(Icons.Filled.InvertColors, contentDescription = null) }
            ),
            SensorCardData(
                title = "ORP",
                valueText = "16.9 mV",
                subtitle = "Chequeo semanal",
                icon = { Icon(Icons.Filled.Speed, contentDescription = null) }
            )
        )
    }

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CropHeaderCard(crop = currentCrop)

        GrowthCard(
            currentDay = currentDay,
            totalDays = totalDays,
            daysUi = daysUi,
            onSelectDay = { }
        )

        SensorsCarousel(cards = sensors)
    }
}

@Composable
private fun CropHeaderCard(crop: Crop) {
    val surface = MaterialTheme.colorScheme.surface
    val overlay = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)

    Card(
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                Spacer(Modifier.width(8.dp))
                Text("Hydrobox", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                AssistChip(onClick = { /* notificaciones */ }, label = { Text("Alerta") })
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = Brush.verticalGradient(listOf(surface, overlay)))
            ) {
                Image(
                    painter = painterResource(id = crop.imageRes),
                    contentDescription = crop.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Text(
                    crop.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GrowthCard(
    currentDay: Int,
    totalDays: Int,
    daysUi: List<DayStep>,
    onSelectDay: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(daysUi) { index, item ->
                    val isSelected = index == (daysUi.size / 2) // demo
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDay(index) },
                        label = { Text(item.label) }
                    )
                }
            }

            val progress = (currentDay.coerceIn(0, totalDays).toFloat() / totalDays.toFloat())
            Text("Día $currentDay de $totalDays", style = MaterialTheme.typography.labelLarge)
            LinearProgressIndicator(
                progress = { progress },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Text(
                "Hoy tus cultivos comienzan a mostrar señales de madurez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SensorsCarousel(cards: List<SensorCardData>) {
    val pagerState = rememberPagerState(pageCount = { cards.size })

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            HorizontalPager(state = pagerState, pageSpacing = 12.dp) { page ->
                SensorCard(cards[page])
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(cards.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorCard(data: SensorCardData) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.icon()
                Spacer(Modifier.width(8.dp))
                Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                data.valueText,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)
            )
            Text(
                data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            data.percent?.let { p ->
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { p.coerceIn(0f, 1f) },
                    trackColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Text("${(p * 100).roundToInt()} %", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
