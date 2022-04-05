package com.moegirlviewer.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.moegirlviewer.util.printDebugLog

@Composable
fun StatusBar(
  visible: Boolean = true,
  backgroundColor: Color = Color.Transparent,
  darkIcons: Boolean = false
) {
  val systemUiController = rememberSystemUiController()

  SideEffect {
//    if (
//      visible != CachedStatusBarConfig.visible ||
//      backgroundColor != CachedStatusBarConfig.backgroundColor ||
//      darkIcons != CachedStatusBarConfig.darkIcons
//    ) {
//      printDebugLog("run")
      systemUiController.isStatusBarVisible = visible
      systemUiController.setStatusBarColor(
        color = backgroundColor,
        darkIcons = darkIcons
      )

//      with(CachedStatusBarConfig) {
//        this.visible = visible
//        this.backgroundColor = backgroundColor
//        this.darkIcons = darkIcons
//      }
//    }
  }
}

private object CachedStatusBarConfig {
  var visible = true
  var backgroundColor = Color.Transparent
  var darkIcons = false
}