package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Speed
import com.hydrobox.app.api.HydroApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hydrobox.app.R
import kotlin.math.absoluteValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.derivedStateOf
import android.content.Context

// Ahora incluye rangos de ORP y Nivel del agua
private data class CropSpec(
    val remoteId: Int,
    val code: String,
    val name: String,
    val imageRes: Int,
    val totalDays: Int,
    val airTempRange: String,
    val humidityRange: String,
    val waterTempRange: String,
    val phRange: String,
    val orpRange: String,
    val levelRange: String
)

// Valores de ORP / Nivel puedes ajustarlos luego si lo deseas
private val cropsCatalog = listOf(
    CropSpec(
        1, "lechuga",  "Lechuga",  R.drawable.crop_lechuga,  20,
        airTempRange = "22°–25°",
        humidityRange = "50–70 %",
        waterTempRange = "22°–25°",
        phRange = "5.8–6.5",
        orpRange = "250–400 mV",
        levelRange = "80–100 %"
    ),
    CropSpec(
        2, "espinaca", "Espinaca", R.drawable.crop_espinaca, 30,
        airTempRange = "16°–24°",
        humidityRange = "50–70 %",
        waterTempRange = "20°–23°",
        phRange = "6.0–7.0",
        orpRange = "250–400 mV",
        levelRange = "80–100 %"
    ),
    CropSpec(
        3, "rucula",   "Rúcula",   R.drawable.crop_rucula,   20,
        airTempRange = "18°–24°",
        humidityRange = "50–70 %",
        waterTempRange = "20°–23°",
        phRange = "6.0–6.8",
        orpRange = "250–400 mV",
        levelRange = "80–100 %"
    ),
    CropSpec(
        4, "acelga",   "Acelga",   R.drawable.crop_acelga,   35,
        airTempRange = "18°–24°",
        humidityRange = "50–70 %",
        waterTempRange = "20°–23°",
        phRange = "6.0–7.0",
        orpRange = "250–400 mV",
        levelRange = "80–100 %"
    ),
    CropSpec(
        5, "albahaca", "Albahaca", R.drawable.crop_albahaca, 21,
        airTempRange = "22°–30°",
        humidityRange = "40–60 %",
        waterTempRange = "22°–25°",
        phRange = "5.5–6.5",
        orpRange = "250–400 mV",
        levelRange = "80–100 %"
    ),
    CropSpec(
        6, "mostaza",  "Mostaza",  R.drawable.crop_mostaza,  25,
        airTempRange = "15°–25°",
        humidityRange = "50–70 %",
        waterTempRange = "18°–22°",
        phRange = "6.0–7.0",
        orpRange = "250–400 mV",
        levelRange = "80–100 %"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CropsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("crop_cycle_prefs", Context.MODE_PRIVATE)
    }
    val scrollState = rememberScrollState()

    var activeIndex by remember { mutableIntStateOf(0) }
    var activeDaysElapsed by remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(pageCount = { cropsCatalog.size })
    var confirmForIndex by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Ahora priorizamos lo guardado en SharedPreferences.
    LaunchedEffect(Unit) {
        val savedId = prefs.getInt("remote_id", -1).takeIf { it != -1 }
        var savedStart = prefs.getLong("start_epoch", -1L).takeIf { it > 0L }

        var apiError: String? = null
        val actual = try {
            HydroApi.getHortalizaActual()
        } catch (e: Exception) {
            apiError = "No se pudo cargar la hortaliza actual"
            null
        }

        // 1) Preferimos lo que está guardado localmente
        val chosenId = savedId ?: actual?.id

        if (chosenId != null) {
            // Si no había start_epoch lo inicializamos ahora
            if (savedStart == null) {
                savedStart = System.currentTimeMillis()
                prefs.edit()
                    .putInt("remote_id", chosenId)
                    .putLong("start_epoch", savedStart!!)
                    .apply()
            }

            val idx = cropsCatalog.indexOfFirst { it.remoteId == chosenId }
            if (idx != -1) {
                activeIndex = idx
                activeDaysElapsed = computeDaysElapsed(savedStart!!)
                pagerState.scrollToPage(idx)
            }
        } else {
            // Primer arranque y API caída → fallback a la primera hortaliza
            val fallbackId = cropsCatalog.first().remoteId
            val now = System.currentTimeMillis()
            prefs.edit()
                .putInt("remote_id", fallbackId)
                .putLong("start_epoch", now)
                .apply()
            activeIndex = 0
            activeDaysElapsed = 1
            pagerState.scrollToPage(0)
        }

        errorMsg = apiError
        loading = false
    }

    Column(
        Modifier
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Gestión de hortalizas",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (loading) {
            Text(
                "Cargando hortaliza actual...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (errorMsg != null) {
            Text(
                errorMsg!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 28.dp),
                pageSize = PageSize.Fill
            ) { page ->
                val crop = cropsCatalog[page]
                val rawOffset = (pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction
                val absOffset = rawOffset.absoluteValue
                val t = 1f - absOffset.coerceIn(0f, 1f)

                val scale = 0.95f + 0.05f * t
                val alpha = 0.85f + 0.15f * t
                val haloAlpha by animateFloatAsState(
                    if (page == pagerState.currentPage) 0.22f else 0f
                )

                val borderBrush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .graphicsLayer {
                            translationX = rawOffset * 60f
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(22.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = haloAlpha),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                )
                            )
                        )
                        .borderIf(
                            page == pagerState.currentPage,
                            width = 1.2.dp,
                            brush = borderBrush,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .noRippleClickable {
                            if (page != activeIndex) {
                                val remaining =
                                    cropsCatalog[activeIndex].totalDays - activeDaysElapsed
                                if (remaining > 0) {
                                    confirmForIndex = page
                                } else {
                                    activeIndex = page
                                    activeDaysElapsed = 1
                                    // si quisieras persistir sin dialog, aquí
                                }
                            }
                        }
                ) {
                    val ctx = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(ctx)
                            .data(crop.imageRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = crop.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            PagerFancyIndicator(
                count = cropsCatalog.size,
                current = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        AnimatedVisibility(visible = pagerState.currentPage != activeIndex) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Toca la imagen para seleccionar") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Opacity,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        val currentCrop = cropsCatalog[pagerState.currentPage]

        // AHORA: 6 tarjetas, una por cada sensor con su rango ideal
        val cards = remember(currentCrop) {
            listOf(
                SensorCardData(
                    title = "Temperatura del aire",
                    valueText = currentCrop.airTempRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.DeviceThermostat, null) }
                ),
                SensorCardData(
                    title = "Humedad del aire",
                    valueText = currentCrop.humidityRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.Opacity, null) }
                ),
                SensorCardData(
                    title = "Temperatura del agua",
                    valueText = currentCrop.waterTempRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.Speed, null) }
                ),
                SensorCardData(
                    title = "pH del agua",
                    valueText = currentCrop.phRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.InvertColors, null) }
                ),
                SensorCardData(
                    title = "ORP",
                    valueText = currentCrop.orpRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.Speed, null) }
                ),
                SensorCardData(
                    title = "Nivel del agua",
                    valueText = currentCrop.levelRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.Opacity, null) }
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TimeEstimateInfoCard(
                isActive = (pagerState.currentPage == activeIndex),
                daysElapsed = activeDaysElapsed,
                totalDays = currentCrop.totalDays
            )

            SensorsCarouselClone(
                cards = cards,
                isCompact = pagerState.currentPage != activeIndex
            )
        }
    }

    val pendingIndex = confirmForIndex
    if (pendingIndex != null) {
        val pendingCrop = cropsCatalog[pendingIndex]
        val activeCrop = cropsCatalog[activeIndex]
        val remaining = (activeCrop.totalDays - activeDaysElapsed).coerceAtLeast(0)

        AlertDialog(
            onDismissRequest = { confirmForIndex = null },
            title = { Text("Cambiar a ${pendingCrop.name}?") },
            text = {
                Text(
                    "Actualmente tienes ${activeCrop.name} con $remaining días pendientes para completar su ciclo. " +
                            "Si cambias ahora, se dará por abandonada.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val ok = HydroApi.cambiarHortaliza(pendingCrop.remoteId)
                                if (ok) {
                                    val now = System.currentTimeMillis()
                                    prefs.edit()
                                        .putInt("remote_id", pendingCrop.remoteId)
                                        .putLong("start_epoch", now)
                                        .apply()

                                    activeIndex = pendingIndex
                                    activeDaysElapsed = 1
                                    pagerState.scrollToPage(pendingIndex)
                                }
                            } catch (_: Exception) {
                                // aquí podrías mostrar un snackbar
                            } finally {
                                confirmForIndex = null
                            }
                        }
                    }
                ) {
                    Text("Abandonar y cambiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmForIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TimeEstimateInfoCard(
    isActive: Boolean,
    daysElapsed: Int,
    totalDays: Int
) {
    val progress = if (isActive) {
        (daysElapsed.coerceIn(0, totalDays).toFloat() /
                totalDays.toFloat().coerceAtLeast(1f))
    } else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Tiempo estimado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Esta hortaliza toma un total de $totalDays días en cultivar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isActive) {
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
                    "Día $daysElapsed de $totalDays",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PagerFancyIndicator(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val selected = i == current
            val w by animateDpAsState(if (selected) 22.dp else 8.dp)
            val alpha by animateFloatAsState(if (selected) 1f else 0.45f)
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                Modifier
                    .height(6.dp)
                    .width(w)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SensorsCarouselClone(
    cards: List<SensorCardData>,
    isCompact: Boolean
) {
    val expandedContent = 140.dp
    val compactContent  = 120.dp

    val animatedContentHeight by animateDpAsState(
        targetValue = if (isCompact) compactContent else expandedContent,
        label = "sensorContentHeight"
    )
    val cardHeight = animatedContentHeight + 32.dp

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(cardHeight)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .height(animatedContentHeight)
            ) {
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
                    pageSize = PageSize.Fixed(baseWidth),
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageOffset = ((pagerState.currentPage - page) +
                            pagerState.currentPageOffsetFraction).absoluteValue
                    val t = 1f - pageOffset.coerceIn(0f, 1f)
                    val scale = 0.96f + 0.04f * t
                    val alpha = 0.85f + 0.15f * t

                    SensorCardClone(
                        data = cards[page],
                        compact = isCompact,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorCardClone(
    data: SensorCardData,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val pad by animateDpAsState(if (compact) 12.dp else 14.dp, label = "pad")
    val spacing by animateDpAsState(if (compact) 4.dp else 6.dp, label = "spacing")
    val meterHeight by animateDpAsState(if (compact) 4.dp else 6.dp, label = "meterH")

    val titleStyle =
        if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val valueStyle =
        if (compact) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        else MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            Modifier.padding(pad),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.icon()
                Spacer(Modifier.width(8.dp))
                Text(
                    data.title,
                    style = titleStyle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                data.valueText,
                style = valueStyle,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(spacing))
            Box(Modifier.fillMaxWidth().height(meterHeight))
        }
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.then(
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick
        )
    )
}

private fun Modifier.borderIf(
    condition: Boolean,
    width: Dp,
    brush: Brush,
    shape: Shape
): Modifier =
    if (condition) this.then(Modifier.border(width, brush, shape)) else this

private fun computeDaysElapsed(startEpoch: Long, now: Long = System.currentTimeMillis()): Int {
    val millisPerDay = 86_400_000L
    val diff = (now - startEpoch).coerceAtLeast(0L)
    val days = (diff / millisPerDay).toInt() + 1
    return days.coerceAtLeast(1)
}
