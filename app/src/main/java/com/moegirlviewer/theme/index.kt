package com.moegirlviewer.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.NospzGothicMoeFamily
import com.moegirlviewer.util.isMoegirl

@Composable
fun MoegirlPlusTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val useSpecialCharSupportedFontInApp by SettingsStore.common.getValue { this.useSpecialCharSupportedFontInApp }.collectAsState(
    initial = false
  )
  val colors = isMoegirl(
    if (isUseDarkMode()) MoegirlDarkColorPalette else MoegirlLightColorPalette,
    if (isUseDarkMode()) HmoeDarkColorPalette else HmoeLightColorPalette
  )
  val typography = remember(useSpecialCharSupportedFontInApp) {
    Typography(
      body1 = Typography.body1.copy(
        fontFamily = if (useSpecialCharSupportedFontInApp) NospzGothicMoeFamily else FontFamily.Default
      )
    )
  }
  val textSelectionColors = remember(colors) {
    TextSelectionColors(
      backgroundColor = colors.secondary.copy(alpha = 0.3f),
      handleColor = colors.secondary,
    )
  }

  MaterialTheme(
    colors = colors,
    typography = typography,
    shapes = Shapes,
    content = {
      CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors,
        content = content,
      )
    }
  )
}