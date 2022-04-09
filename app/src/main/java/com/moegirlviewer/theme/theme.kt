package com.moegirlviewer.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.moegirlviewer.util.isMoegirl

@SuppressLint("ConflictingOnColor")
val MoegirlLightColorPalette = lightColors(
  primary = GreenPrimary,       // 主颜色
  primaryVariant = GreenPrimary,  // 强调颜色，深色主题时等于主颜色，纯白主题时等于app默认主题色，黑暗模式时等于app黑暗对比色
  secondary = GreenPrimary,     // 可交互内容颜色，目前等同于强调颜色，另：不要使用contentColor()
  background = Color.White,
  error = RedAccent,

  onPrimary = Color.White,
  onSecondary = Color.White,
  surface = Color.White,
)

@SuppressLint("ConflictingOnColor")
val MoegirlDarkColorPalette = darkColors(
  primary = Color(0xff3A3A3B),
  primaryVariant = GreenSecondary,
  secondary = GreenSecondary,
  background = Color(0xff252526),
  surface = Color(0xff3A3A3B),

  onPrimary = Color(0xffBFBFBF),
  onSecondary = Color(0xffBFBFBF),
  onSurface = Color(0xffBFBFBF)
)


@SuppressLint("ConflictingOnColor")
val HmoeLightColorPalette = lightColors(
  primary = OrangePrimary,
  primaryVariant = OrangePrimary,
  secondary = OrangePrimary,
  background = Color.White,
  error = RedAccent,

  onPrimary = Color.White,
  onSecondary = Color.White,
  surface = Color.White,
)

@SuppressLint("ConflictingOnColor")
val HmoeDarkColorPalette = darkColors(
  primary = Color(0xff3A3A3B),
  primaryVariant = Color(0xffffE686),
  secondary = Color(0xffffE686),
  background = Color(0xff252526),
  surface = Color(0xff3A3A3B),

  onPrimary = Color(0xffBFBFBF),
  onSecondary = Color(0xffBFBFBF),
  onSurface = Color(0xffBFBFBF)
)

@SuppressLint("ConflictingOnColor")
@Composable
fun getPureColorPalette(): Colors {
  val themeTextColors = MaterialTheme.colors.text

  return remember {
    lightColors(
      primary = Color.White,
      primaryVariant = isMoegirl(GreenPrimary, OrangePrimary),
      secondary = isMoegirl(GreenPrimary, OrangePrimary),

      onPrimary = Color(0xff666666),
      onSecondary = Color.White
    )
  }
}

class TextColors(
  val primary: Color,
  val secondary: Color,
  val tertiary: Color
)

val MaterialTheme.elevation @Composable get() = isUsePureTheme()

// 文字主题正确方式是应该用Material.Typography和LocalTextStyle的，刚开始没搞懂，现在要改工作量太大，就这样吧_(:з」∠)_
val Colors.text
  @Composable get() =
    if (!isUseDarkMode()) TextColors(
      primary = TextPrimary,
      secondary = TextSecondary,
      tertiary = TextTertiary
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
