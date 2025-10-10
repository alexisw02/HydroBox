package com.hydrobox.app.ui.model

import androidx.annotation.DrawableRes
import com.hydrobox.app.R

enum class Crop(val displayName: String, @DrawableRes val imageRes: Int) {
    ACELGA("Acelga", R.drawable.crop_acelga),
    ALBAHACA("Albahaca", R.drawable.crop_albahaca),
    ESPINACA("Espinaca", R.drawable.crop_espinaca),
    LECHUGA("Lechuga", R.drawable.crop_lechuga),
    MOSTAZA("Mostaza", R.drawable.crop_mostaza),
    RUCULA("Rúcula", R.drawable.crop_rucula)
}