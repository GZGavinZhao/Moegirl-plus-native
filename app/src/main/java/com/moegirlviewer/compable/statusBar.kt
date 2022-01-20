package com.moegirlviewer.compable

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun StatusBar(
  backgroundColor: Color = Color.Transparent,
  darkIcons: Boolean = false
) {
  val systemUiController = rememberSystemUiController()

  LaunchedEffect(true) {
    systemUiController.setStatusBarColor(
      color = backgroundColor,
      darkIcons = darkIcons
    )
  }
}