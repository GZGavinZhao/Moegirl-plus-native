package com.moegirlviewer.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.moegirlviewer.util.darken
import com.moegirlviewer.util.lighten

@SuppressLint("ConflictingOnColor")
val MoegirlLightColorPalette = lightColors(
  primary = GreenPrimary,
  primaryVariant = GreenLight,
  secondary = GreenPrimary,
  secondaryVariant = GreenLight,
  background = Color.White,
  error = RedAccent,

  onPrimary = Color.White,
  surface = Color.White,
)

@SuppressLint("ConflictingOnColor")
val MoegirlDarkColorPalette = darkColors(
  primary = Color(0xff3A3A3B),
  primaryVariant = Color(0xff3A3A3B).lighten(0.2f),
  secondary = GreenSecondary,
  secondaryVariant = GreenSecondary.darken(0.2f),
  background = Color(0xff252526),
  surface = Color(0xff3A3A3B),

  onPrimary = Color(0xffBFBFBF),
  onSurface = Color(0xffBFBFBF)
)


@SuppressLint("ConflictingOnColor")
val HmoeLightColorPalette = lightColors(
  primary = OrangePrimary,
  primaryVariant = OrangeLight,
  secondary = OrangePrimary,
  secondaryVariant = OrangeLight,
  background = Color.White,
  error = RedAccent,

  onPrimary = Color.White,
//  onSurface = Color(0xff323232),

//  onSecondary = Color.White,
  surface = Color.White,
//  onBackground = Color.Black,
)

@SuppressLint("ConflictingOnColor")
val HmoeDarkColorPalette = darkColors(
  primary = Color(0xff3A3A3B),
  primaryVariant = Color(0xff3A3A3B).lighten(0.2f),
  secondary = Color(0xffffE686),
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
val Colors.text
  @Composable get() =
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
val Colors.background2
  @Composable get() =
    if (!isUseDarkMode())
      Color(0xffeeeeee) else
      this.background

enum class ColorThemes {
  DARK
}

@Composable
fun isUseDarkMode(): Boolean {
// 目前compose的text组件对于黑暗模式下仍旧使用浅色主题的适配好像有问题，无论主题是否是light类型，文字都会被强制反转色相，这里先注释
//  val darkThemeBySystem by SettingsStore.common.getValue { this.darkThemeBySystem }.collectAsState(initial = false)
//  return isSystemInDarkTheme() && darkThemeBySystem
  return isSystemInDarkTheme()
//  return false
}
