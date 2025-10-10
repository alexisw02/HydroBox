package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.BoxWithConstraints

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.hydrobox.app.ui.model.Crop
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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
    val currentCrop = remember { Crop.LECHUGA }

    var currentDay by remember { mutableIntStateOf(12) }
    val totalDays = currentCrop.totalDays

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
            onDayChanged = { currentDay = it }
        )

        SensorsCarousel(cards = sensors)
    }
}

@Composable
private fun DaysScroller(
    totalDays: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val itemWidth = 56.dp
    val spacing = 8.dp
    val listState = rememberLazyListState()

    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val itemPx = with(density) { itemWidth.roundToPx() }
    val spacingPx = with(density) { spacing.roundToPx() }

    // Longitud total de la fila (aprox; suficiente para el cálculo de offset)
    val totalContentPx = totalDays * itemPx + (totalDays - 1) * spacingPx
    val maxOffset = (totalContentPx - screenWidthPx).coerceAtLeast(0)

    LaunchedEffect(selectedDay, totalDays) {
        val i = (selectedDay - 1).coerceIn(0, totalDays - 1)

        // Centro deseado para items intermedios
        val centerOffset = (i * (itemPx + spacingPx)) - (screenWidthPx - itemPx) / 2

        // Clamping: 0 => pegado a la izquierda (día 1), maxOffset => pegado a la derecha (último)
        val targetOffset = centerOffset.coerceIn(0, maxOffset)

        // Truco: desplazamos la lista a partir del primer ítem con un offset absoluto
        listState.animateScrollToItem(index = 0, scrollOffset = targetOffset)
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(totalDays) { index ->
            val day = index + 1
            val selected = day == selectedDay
            FilterChip(
                selected = selected,
                onClick = { onDaySelected(day) },
                label = { Text(day.toString()) },
                modifier = Modifier.width(itemWidth)
            )
        }
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
    onDayChanged: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            DaysScroller(
                totalDays = totalDays,
                selectedDay = currentDay,
                onDaySelected = onDayChanged
            )

            val progress = (currentDay.coerceIn(1, totalDays).toFloat() / totalDays.toFloat())
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SensorsCarousel(cards: List<SensorCardData>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val peek = 28.dp
                val pageWidth = maxWidth - peek * 2
                val pagerState = rememberPagerState(pageCount = { cards.size })

                Column {
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp,
                        contentPadding = PaddingValues(horizontal = peek),
                        pageSize = PageSize.Fixed(pageWidth)
                    ) { page ->
                        val pageOffset = ((pagerState.currentPage - page) +
                                pagerState.currentPageOffsetFraction).absoluteValue

                        val t = 1f - pageOffset.coerceIn(0f, 1f)
                        val scale = 0.96f + 0.04f * t
                        val alpha = 0.85f + 0.15f * t

                        SensorCard(
                            data = cards[page],
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                        )
                    }

                    // Dots usando el mismo pagerState
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val current by remember { derivedStateOf { pagerState.currentPage } }
                        repeat(cards.size) { index ->
                            val selected = current == index
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

            Text(
                "Desliza para ver más",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SensorCard(
    data: SensorCardData,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = modifier
            .heightIn(min = 150.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.icon()
                Spacer(Modifier.width(8.dp))
                Text(data.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(data.valueText, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black))
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

