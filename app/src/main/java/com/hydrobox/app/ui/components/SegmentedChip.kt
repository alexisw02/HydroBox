package com.hydrobox.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SegmentedVariant { Primary, Tonal }

@Composable
fun SegmentedChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    variant: SegmentedVariant = SegmentedVariant.Primary
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(999.dp)

    // Colores según variante y estado
    val (bg, fg, stroke) = when (variant) {
        SegmentedVariant.Primary -> {
            if (selected)
                Triple(cs.primary.copy(alpha = 0.18f), cs.onPrimaryContainer, cs.primary.copy(alpha = 0.50f))
            else
                Triple(cs.surfaceVariant.copy(alpha = 0.30f), cs.onSurfaceVariant, cs.onSurfaceVariant.copy(alpha = 0.35f))
        }
        SegmentedVariant.Tonal -> {
            if (selected)
                Triple(cs.surfaceVariant.copy(alpha = 0.55f), cs.onSurface, cs.primary.copy(alpha = 0.35f))
            else
                Triple(cs.surfaceVariant.copy(alpha = 0.22f), cs.onSurfaceVariant, cs.onSurfaceVariant.copy(alpha = 0.30f))
        }
    }

    Box(
        modifier = Modifier
            .background(bg, shape)
            .border(1.dp, stroke, shape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        )
    }
}
