package com.hydrobox.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun HydroCard(
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = MaterialTheme.colorScheme.surface
    val strokeA = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val strokeB = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    0f to bg.copy(alpha = 0.92f),
                    1f to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(strokeA, strokeB)),
                shape = shape
            )
            .padding(16.dp)
    ) {
        if (!title.isNullOrEmpty()) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (!subtitle.isNullOrEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!title.isNullOrEmpty() || !subtitle.isNullOrEmpty()) {
            Spacer(Modifier.height(12.dp))
        }
        content()
    }
}
