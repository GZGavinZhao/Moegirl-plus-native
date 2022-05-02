package com.moegirlviewer.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontFamily
import com.moegirlviewer.store.CommonSettings
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.NospzGothicMoeFamily
import com.moegirlviewer.util.isMoegirl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.forEach

@Composable
fun MoegirlPlusTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val useSpecialCharSupportedFontInApp by SettingsStore.common.getValue { this.useSpecialCharSupportedFontInApp }.collectAsState(
    initial = false
  )

  val colors = when {
    isUseDarkMode() -> isMoegirl(MoegirlDarkColorPalette, HmoeDarkColorPalette)
    isUsePureTheme() -> getPureColorPalette()
    else -> isMoegirl(MoegirlLightColorPalette, HmoeLightColorPalette)
  }

  _currentThemeColors = colors
  _currentIsUseDarkTheme = isUseDarkMode()

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
//    shapes = if (isUsePureTheme()) PureThemeShapes else ColorfulThemeShapes,
    content = {
      CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors,
        content = content,
      )
    }
  )
}

private var cachedUsePureTheme by mutableStateOf(false)
@Composable
fun isUsePureTheme(): Boolean {
  LaunchedEffect(true) {
    SettingsStore.common.getValue { this.usePureTheme }
      .collect { cachedUsePureTheme = it }
  }

  return cachedUsePureTheme
}

private var _currentThemeColors: Colors? = null
private var _currentIsUseDarkTheme: Boolean? = null

val currentThemeColors get() = _currentThemeColors!!
val currentIsUseDarkTheme get() = _currentIsUseDarkTheme!!