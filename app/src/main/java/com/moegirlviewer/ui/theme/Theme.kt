package com.moegirlviewer.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.moegirlviewer.extants.darken
import com.moegirlviewer.extants.lighten
import com.moegirlviewer.extants.toCssRgbaString
import com.moegirlviewer.store.SettingsStore

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
  primary = GreenPrimary,
  primaryVariant = GreenLight,
  secondary = GreenPrimary,
  secondaryVariant = GreenLight,
  background = Color.White,
  error = RedAccent,

  onPrimary = Color.White,
//  onSurface = Color(0xff323232),

//  onSecondary = Color.White,
  surface = Color.White,
//  onBackground = Color.Black,
)

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
  primary = Color(0xff3A3A3B),
  primaryVariant = Color(0xff3A3A3B).lighten(0.2f),
  secondary = GreenSecondary,
  secondaryVariant = GreenSecondary.darken(0.2f),
  background = Color(0xff252526),
  surface = Color(0xff3A3A3B),

  onPrimary = Color(0xffBFBFBF),
  onSurface = Color(0xffBFBFBF)
)


class TextColors(
  val primary: Color,
  val secondary: Color,
  val tertiary: Color
)

// 文字主题正确方式是应该用Material.Typography和LocalTextStyle的，刚开始没搞懂，现在要改工作量太大，就这样吧_(:з」∠)_
val Colors.text @Composable get() =
  if (!isUseDarkMode()) TextColors(
    primary = Color(0xff323232),
    secondary = Color(0xff797979),
    tertiary = Color(0xffD0D0D0)
  ) else TextColors(
    primary = Color(0xffBFBFBF),
    secondary = Color(0xFFA2A2A2),
    tertiary = Color(0xFF615F5F)
  )

// 用于列表页面的灰色背景
val Colors.background2 @Composable get() =
  if (!isUseDarkMode())
    Color(0xffeeeeee) else
    this.background

@Composable
fun MoegirlPlusTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colors = if (isUseDarkMode()) {
    DarkColorPalette
  } else {
    LightColorPalette
  }
  
  val textSelectionColors = TextSelectionColors(
    backgroundColor = colors.secondary.copy(alpha = 0.5f),
    handleColor = colors.secondary,
  )

  MaterialTheme(
    colors = colors,
    typography = Typography,
    shapes = Shapes,
    content = {
      CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors,
        content = content,
      )
    }
  )
}

enum class ColorThemes {
  DARK
}

@Composable
private fun isUseDarkMode(): Boolean {
// 目前compose的text组件对于黑暗模式下仍旧使用浅色主题的适配好像有问题，设置的黑色会被处理得很浅，这里先注释
//  val darkThemeBySystem by SettingsStore.common.getValue { this.darkThemeBySystem }.collectAsState(initial = false)
//  return isSystemInDarkTheme() && darkThemeBySystem
  return isSystemInDarkTheme()
}
