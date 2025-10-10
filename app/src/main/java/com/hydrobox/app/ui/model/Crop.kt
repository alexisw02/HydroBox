package com.hydrobox.app.ui.model

import androidx.annotation.DrawableRes
import com.hydrobox.app.R

enum class Crop(
    val displayName: String,
    @DrawableRes val imageRes: Int,
    val totalDays: Int
    ) {
    ACELGA("Acelga", R.drawable.crop_acelga, 28),
    ALBAHACA("Albahaca", R.drawable.crop_albahaca, 30),
    ESPINACA("Espinaca", R.drawable.crop_espinaca, 32),
    LECHUGA("Lechuga", R.drawable.crop_lechuga, 45),
    MOSTAZA("Mostaza", R.drawable.crop_mostaza, 26),
    RUCULA("Rúcula", R.drawable.crop_rucula, 30)
}