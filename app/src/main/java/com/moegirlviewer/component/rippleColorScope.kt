package com.moegirlviewer.component

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun RippleColorScope(
  color: Color,
  content: @Composable () -> Unit,
) {
  val rippleThemeImpl = remember {
    object : RippleTheme {
      @Composable
      override fun defaultColor() =
        RippleTheme.defaultRippleColor(
          contentColor = color,
          lightTheme = true
        )

      @Composable
      override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(
          contentColor = color,
          lightTheme = true
        )
    }
  }

  CompositionLocalProvider(
    LocalRippleTheme provides rippleThemeImpl
  ) {
    content()
  }
}