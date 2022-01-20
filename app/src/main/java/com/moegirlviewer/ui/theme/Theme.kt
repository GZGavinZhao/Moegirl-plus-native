package com.moegirlviewer.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.moegirlviewer.extants.darken
import com.moegirlviewer.extants.lighten

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
  primary = GreenPrimary,
  primaryVariant = GreenLight,
  secondary = GreenPrimary,
  secondaryVariant = GreenLight,
  background = Color.White,
  error = RedAccent,

  onPrimary = Color.White,
  onSurface = Color(0xffD0D0D0),

//  onSecondary = Color.White,
//  surface = Color.White,
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
  onSurface = Color(0xffD0D0D0)
)


class TextColors(
  val primary: Color,
  val secondary: Color,
  val tertiary: Color
)

val Colors.text @Composable get() =
  if (true) TextColors(
    primary = Color(0xff323232),
    secondary = Color(0xff797979),
    tertiary = Color(0xffD0D0D0)
  ) else TextColors(
    primary = Color(0xffBFBFBF),
    secondary = Color(0xff797979),
    tertiary = Color(0xffD0D0D0)
  )

// 用于列表页面的灰色背景
val Colors.background2 @Composable get() =
  if (!isSystemInDarkTheme())
    Color(0xffeeeeee) else
    this.background

@Composable
fun MoegirlPlusTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
//  val colors = if (darkTheme) {
//    DarkColorPalette
//  } else {
//    LightColorPalette
//  }
  // 黑暗模式有问题，暂时关闭
  val colors = LightColorPalette

  MaterialTheme(
    colors = colors,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}