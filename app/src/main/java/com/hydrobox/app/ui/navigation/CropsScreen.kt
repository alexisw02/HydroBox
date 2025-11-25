package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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

private data class CropSpec(
    val id: String,
    val name: String,
    val imageRes: Int,
    val totalDays: Int,
    val airTempRange: String,
    val humidityRange: String,
    val waterTempRange: String,
    val phRange: String
)

private val cropsCatalog = listOf(
    CropSpec("lechuga", "Lechuga", R.drawable.crop_lechuga, 20, "22°–25°", "50–70 %", "22°–25°", "5.8–6.5"),
    CropSpec("espinaca", "Espinaca", R.drawable.crop_espinaca, 30, "16°–24°", "50–70 %", "20°–23°", "6.0–7.0"),
    CropSpec("rucula", "Rúcula", R.drawable.crop_rucula, 20, "18°–24°", "50–70 %", "20°–23°", "6.0–6.8"),
    CropSpec("acelga", "Acelga", R.drawable.crop_acelga, 35, "18°–24°", "50–70 %", "20°–23°", "6.0–7.0"),
    CropSpec("albahaca", "Albahaca", R.drawable.crop_albahaca, 21, "22°–30°", "40–60 %", "22°–25°", "5.5–6.5"),
    CropSpec("mostaza", "Mostaza", R.drawable.crop_mostaza, 25, "15°–25°", "50–70 %", "18°–22°", "6.0–7.0")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CropsScreen(paddingValues: PaddingValues) {
    val scrollState = rememberScrollState()

    var activeIndex by remember { mutableIntStateOf(0) }
    var activeDaysElapsed by remember { mutableIntStateOf(12) }
    val pagerState = rememberPagerState(pageCount = { cropsCatalog.size })
    var confirmForIndex by remember { mutableStateOf<Int?>(null) }

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

        Box {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 28.dp),
                pageSize = PageSize.Fill
            ) { page ->
                val crop = cropsCatalog[page]
                val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = rawOffset.absoluteValue
                val t = 1f - absOffset.coerceIn(0f, 1f)

                val scale = 0.95f + 0.05f * t
                val alpha = 0.85f + 0.15f * t
                val haloAlpha by animateFloatAsState(if (page == pagerState.currentPage) 0.22f else 0f)

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
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp), clip = false)
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
                                val remaining = cropsCatalog[activeIndex].totalDays - activeDaysElapsed
                                if (remaining > 0) confirmForIndex = page
                                else {
                                    activeIndex = page
                                    activeDaysElapsed = 0
                                    // TODO: persistir selección
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
                leadingIcon = { Icon(Icons.Filled.Opacity, null, modifier = Modifier.size(16.dp)) }
            )
        }

        val currentCrop = cropsCatalog[pagerState.currentPage]
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
                )
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TimeEstimateInfoCard(
                isActive    = (pagerState.currentPage == activeIndex),
                daysElapsed = activeDaysElapsed,
                totalDays   = currentCrop.totalDays
            )

            SensorsCarouselClone(
                cards     = cards,
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
                TextButton(onClick = {
                    activeIndex = pendingIndex
                    activeDaysElapsed = 0
                    confirmForIndex = null
                    // TODO: persistir selección
                }) { Text("Abandonar y cambiar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmForIndex = null }) { Text("Cancelar") }
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
        (daysElapsed.coerceIn(0, totalDays).toFloat() / totalDays.toFloat().coerceAtLeast(1f))
    } else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tiempo estimado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                Text("Día $daysElapsed de $totalDays", style = MaterialTheme.typography.labelLarge)
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
    // ↓ valor más contenido en compacto para que no empuje la altura
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
