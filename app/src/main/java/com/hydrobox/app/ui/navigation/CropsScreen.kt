package com.hydrobox.app.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hydrobox.app.R
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.derivedStateOf

/* ============================ Modelo simple local ============================ */

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

/** Mapea a tus drawables en res/drawable */
private val cropsCatalog = listOf(
    CropSpec("lechuga", "Lechuga", R.drawable.crop_lechuga, 20, "22°–25°", "50–70 %", "22°–25°", "5.8–6.5"),
    CropSpec("espinaca", "Espinaca", R.drawable.crop_espinaca, 30, "16°–24°", "50–70 %", "20°–23°", "6.0–7.0"),
    CropSpec("rucula", "Rúcula", R.drawable.crop_rucula, 20, "18°–24°", "50–70 %", "20°–23°", "6.0–6.8"),
    CropSpec("acelga", "Acelga", R.drawable.crop_acelga, 35, "18°–24°", "50–70 %", "20°–23°", "6.0–7.0"),
    CropSpec("albahaca", "Albahaca", R.drawable.crop_albahaca, 21, "22°–30°", "40–60 %", "22°–25°", "5.5–6.5"),
    CropSpec("mostaza", "Mostaza", R.drawable.crop_mostaza, 25, "15°–25°", "50–70 %", "18°–22°", "6.0–7.0")
)

/* =============================== Pantalla =================================== */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CropsScreen(paddingValues: PaddingValues) {
    // Estado real (en producción vendrá de DB)
    var activeIndex by remember { mutableIntStateOf(0) }
    var activeDaysElapsed by remember { mutableIntStateOf(12) } // solo para validación/visualización

    val pagerState = rememberPagerState(pageCount = { cropsCatalog.size })
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { pagerState.scrollToPage(activeIndex) }

    // Confirmación si hay cultivo sin terminar
    var confirmForIndex by remember { mutableStateOf<Int?>(null) }

    // “Lo que esté al centro” es la selección tentativa → dispara confirmación si aplica
    LaunchedEffect(pagerState.currentPage) {
        val newIndex = pagerState.currentPage
        if (newIndex != activeIndex) {
            val remaining = cropsCatalog[activeIndex].totalDays - activeDaysElapsed
            if (remaining > 0) {
                confirmForIndex = newIndex
            } else {
                activeIndex = newIndex
                activeDaysElapsed = 0
                // TODO: Persistir selección en DB
            }
        }
    }

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Gestión de hortalizas",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        /* ========== Carrusel principal: SOLO imagen ========== */
        HorizontalPager(
            state = pagerState,
            pageSpacing = 14.dp,
            contentPadding = PaddingValues(horizontal = 28.dp),
            pageSize = PageSize.Fill
        ) { page ->
            val crop = cropsCatalog[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val t = 1f - pageOffset.coerceIn(0f, 1f)
            val scale = 0.96f + 0.04f * t
            val alpha = 0.90f + 0.10f * t

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            ) {
                AsyncImage(
                    model = crop.imageRes,
                    contentDescription = crop.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        /* ========== Tiempo estimado (SOLO INFO) ========== */
        TimeEstimateInfoCard(
            isActive = (pagerState.currentPage == activeIndex),
            daysElapsed = activeDaysElapsed,
            totalDays = cropsCatalog[pagerState.currentPage].totalDays
        )

        /* ========== Rangos ideales (idéntico a ResumeScreen) ========== */
        val currentCrop = cropsCatalog[pagerState.currentPage]
        val cards = remember(currentCrop) {
            listOf(
                SensorCardData(
                    title = "Temperatura del aire",
                    valueText = currentCrop.airTempRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.DeviceThermostat, contentDescription = null) }
                ),
                SensorCardData(
                    title = "Humedad del aire",
                    valueText = currentCrop.humidityRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.Opacity, contentDescription = null) }
                ),
                SensorCardData(
                    title = "Temperatura del agua",
                    valueText = currentCrop.waterTempRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.Speed, contentDescription = null) }
                ),
                SensorCardData(
                    title = "pH del agua",
                    valueText = currentCrop.phRange,
                    subtitle = "Rango óptimo",
                    icon = { Icon(Icons.Filled.InvertColors, contentDescription = null) }
                )
            )
        }
        SensorsCarouselClone(cards = cards) // MISMO look&feel que en ResumeScreen
    }

    /* ========== Diálogo de confirmación (con reversión al cancelar) ========== */
    val pendingIndex = confirmForIndex
    if (pendingIndex != null) {
        val pendingCrop = cropsCatalog[pendingIndex]
        val activeCrop = cropsCatalog[activeIndex]
        val remaining = (activeCrop.totalDays - activeDaysElapsed).coerceAtLeast(0)

        AlertDialog(
            onDismissRequest = {
                confirmForIndex = null
                scope.launch { pagerState.animateScrollToPage(activeIndex) }
            },
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
                        activeIndex = pendingIndex
                        activeDaysElapsed = 0
                        confirmForIndex = null
                        scope.launch { pagerState.animateScrollToPage(activeIndex) }
                        // TODO: Persistir selección en DB
                    }
                ) { Text("Abandonar y cambiar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmForIndex = null
                        scope.launch { pagerState.animateScrollToPage(activeIndex) }
                    }
                ) { Text("Cancelar") }
            }
        )
    }
}

/* ======================== Componentes auxiliares ======================== */

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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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

/* ================== CLON del carrusel de sensores (igual a Resume) ================== */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SensorsCarouselClone(cards: List<SensorCardData>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    SensorCardClone(
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
            }
        }
    }
}

@Composable
private fun SensorCardClone(
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
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            Box(Modifier.fillMaxWidth().height(meterHeight)) // sin controles
        }
    }
}
