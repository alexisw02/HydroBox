package com.hydrobox.app.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDarkThemeState = staticCompositionLocalOf<MutableState<Boolean>> {
    error("LocalDarkThemeState not provided")
}
