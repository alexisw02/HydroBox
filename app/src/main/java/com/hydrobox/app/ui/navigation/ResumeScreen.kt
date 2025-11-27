package com.hydrobox.app.ui.navigation

import android.content.Context
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
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hydrobox.app.api.ApiMedicion
import com.hydrobox.app.api.HydroApi
import com.hydrobox.app.ui.model.Crop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val context = LocalContext.current

    var currentCrop by remember { mutableStateOf<Crop?>(null) }
    var totalDays by remember { mutableIntStateOf(0) }
    var startEpoch by remember { mutableStateOf<Long?>(null) }

    // reloj para que el día se actualice solo
    val now by produceState(initialValue = System.currentTimeMillis(), startEpoch) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    // AHORA: usamos primero lo que haya guardado CropsScreen en prefs
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("crop_cycle_prefs", Context.MODE_PRIVATE)

        val savedId = prefs.getInt("remote_id", -1).takeIf { it != -1 }
        var savedStart = prefs.getLong("start_epoch", -1L).takeIf { it > 0L }

        val actual = try {
            HydroApi.getHortalizaActual()
        } catch (_: Exception) {
            null
        }

        val chosenId = savedId ?: actual?.id

        if (chosenId != null) {
            if (savedStart == null) {
                savedStart = System.currentTimeMillis()
                prefs.edit()
                    .putInt("remote_id", chosenId)
                    .putLong("start_epoch", savedStart!!)
                    .apply()
            }
            startEpoch = savedStart
            val crop = cropForRemoteId(chosenId)
            currentCrop = crop
            totalDays = crop.totalDays
        } else {
            // fallback a lechuga si no hay nada ni en prefs ni en API
            val fallbackId = 1
            val now = System.currentTimeMillis()
            prefs.edit()
                .putInt("remote_id", fallbackId)
                .putLong("start_epoch", now)
                .apply()
            startEpoch = now
            val crop = cropForRemoteId(fallbackId)
            currentCrop = crop
            totalDays = crop.totalDays
        }
    }

    val autoDay = remember(startEpoch, now, totalDays) {
        val start = startEpoch
        if (start == null || totalDays <= 0) 1
        else {
            val diffMillis = (now - start).coerceAtLeast(0L)
            val days = (diffMillis / 86_400_000L).toInt() + 1
            days.coerceIn(1, if (totalDays <= 0) Int.MAX_VALUE else totalDays)
        }
    }

    var currentDay by remember(autoDay) { mutableIntStateOf(autoDay) }

    LaunchedEffect(autoDay) {
        if (autoDay > currentDay) currentDay = autoDay
    }

    // --- sensores: último registro de la API cada 15s ---
    var latest by remember { mutableStateOf<ApiMedicion?>(null) }
    var sensorsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val list = HydroApi.getRegistroMediciones()
                latest = list.lastOrNull()
                sensorsError = null
            } catch (e: Exception) {
                sensorsError = "No se pudieron cargar los datos de los sensores."
            }
            delay(15_000L)
        }
    }

    val sensors = remember(latest) {
        val m = latest
        val fecha = m?.fecha ?: "Sin datos"

        fun fmt(value: Float?, suffix: String) =
            if (value != null) String.format("%.1f %s", value, suffix) else "-- $suffix"

        listOf(
            SensorCardData(
                title = "Temperatura del aire",
                valueText = fmt(m?.airTemp, "°C"),
                subtitle = "Última lectura • $fecha",
                icon = { Icon(Icons.Filled.DeviceThermostat, contentDescription = null) },
            ),
            SensorCardData(
                title = "Humedad del aire",
                valueText = fmt(m?.humidity, "%"),
                subtitle = "Última lectura • $fecha",
                icon = { Icon(Icons.Filled.Opacity, contentDescription = null) },
            ),
            SensorCardData(
                title = "Temperatura del agua",
                valueText = fmt(m?.waterTemp, "°C"),
                subtitle = "Última lectura • $fecha",
                icon = { Icon(Icons.Filled.Speed, contentDescription = null) }
            ),
            SensorCardData(
                title = "pH del agua",
                valueText = m?.ph?.let { String.format("%.2f", it) } ?: "--",
                subtitle = "Última lectura • $fecha",
                icon = { Icon(Icons.Filled.InvertColors, contentDescription = null) }
            ),
            SensorCardData(
                title = "ORP",
                valueText = m?.orp?.let { "${it.roundToInt()} mV" } ?: "-- mV",
                subtitle = "Última lectura • $fecha",
                icon = { Icon(Icons.Filled.Speed, contentDescription = null) }
            ),
            SensorCardData(
                title = "Nivel del agua",
                valueText = m?.level?.let { "${it.roundToInt()} %" } ?: "-- %",
                subtitle = "Última lectura • $fecha",
                icon = { Icon(Icons.Filled.Opacity, contentDescription = null) }
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
        val crop = currentCrop
        if (crop == null || totalDays == 0) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            CropHeaderCard(crop = crop)

            GrowthCard(
                currentDay = currentDay,
                totalDays = totalDays,
                onDayChanged = { currentDay = it.coerceIn(1, totalDays) }
            )
        }

        if (sensorsError != null) {
            Text(
                sensorsError!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        SensorsCarousel(cards = sensors)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DaysScroller(
    totalDays: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    if (totalDays <= 0) return

    val itemWidth = 56.dp
    val spacing   = 8.dp

    val startPad  = 0.dp
    val endPad    = 0.dp

    val state        = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val scope        = rememberCoroutineScope()

    val targetIndex = (selectedDay - 1).coerceIn(0, totalDays - 1)

    LaunchedEffect(totalDays) {
        state.scrollToItem(targetIndex)
    }

    LaunchedEffect(targetIndex, totalDays) {
        val near = state.layoutInfo.visibleItemsInfo
            .any { it.index == targetIndex && it.offset == 0 }
        if (!near) state.animateScrollToItem(targetIndex)
    }

    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(start = startPad, end = endPad),
        flingBehavior = snapBehavior,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(count = totalDays, key = { it }) { index ->
            val day = index + 1
            FilterChip(
                selected = day == selectedDay,
                onClick = {
                    onDaySelected(day)
                    scope.launch { state.animateScrollToItem(index) }
                },
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
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Hydrobox",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                AssistChip(onClick = { }, label = { Text("Alerta") })
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(surface, overlay)
                        )
                    )
            ) {
                AsyncImage(
                    model = crop.imageRes,
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
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            DaysScroller(
                totalDays = totalDays,
                selectedDay = currentDay,
                onDaySelected = onDayChanged
            )

            val progress = if (totalDays > 0) {
                currentDay.coerceIn(1, totalDays).toFloat() / totalDays.toFloat()
            } else 0f

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
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val peek = 20.dp
                val pageSpacing = 8.dp
                val pagerState = rememberPagerState(pageCount = { cards.size })
                val last = cards.lastIndex
                val current by remember { derivedStateOf { pagerState.currentPage } }

                val baseWidth = maxWidth - peek * 2

                val startPad = if (current == 0) 0.dp else peek
                val endPad   = if (current == last) 0.dp else peek

                HorizontalPager(
                    state = pagerState,
                    pageSpacing = pageSpacing,
                    contentPadding = PaddingValues(start = startPad, end = endPad),
                    pageSize = PageSize.Fixed(baseWidth)
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

                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                ) { }
            }
        }
    }
}

@Composable
private fun SensorCard(
    data: SensorCardData,
    modifier: Modifier = Modifier
) {
    val meterHeight = 6.dp

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = modifier.requiredHeight(140.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.icon()
                Spacer(Modifier.width(8.dp))
                Text(
                    data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                data.valueText,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                maxLines = 1
            )
            Text(
                data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(Modifier.height(6.dp))

            if (data.percent != null) {
                LinearProgressIndicator(
                    progress = { data.percent.coerceIn(0f, 1f) },
                    trackColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(meterHeight)
                        .clip(RoundedCornerShape(6.dp))
                )
                Text(
                    "${(data.percent * 100).roundToInt()} %",
                    style = MaterialTheme.typography.labelMedium
                )
            } else {
                Box(Modifier.fillMaxWidth().height(meterHeight))
            }
        }
    }
}

private fun cropForRemoteId(id: Int): Crop =
    when (id) {
        1 -> Crop.LECHUGA
        2 -> Crop.ESPINACA
        3 -> Crop.RUCULA
        4 -> Crop.ACELGA
        5 -> Crop.ALBAHACA
        6 -> Crop.MOSTAZA
        else -> Crop.LECHUGA
    }
