package com.moegirlviewer.compable

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun StatusBar(
  visible: Boolean = true,
  backgroundColor: Color = Color.Transparent,
  darkIcons: Boolean = false,
) {
  val systemUiController = rememberSystemUiController()
  val statusBarLockedShadow = statusBarLocked   // state必须出现在composable函数的上下文中，才能正确触发组件重渲染

  SideEffect {
    if (statusBarLockedShadow) return@SideEffect

    if (
      visible != CachedStatusBarConfig.visible ||
      backgroundColor != CachedStatusBarConfig.backgroundColor ||
      darkIcons != CachedStatusBarConfig.darkIcons
    ) {
      systemUiController.isStatusBarVisible = visible
      systemUiController.setStatusBarColor(
        color = backgroundColor,
        darkIcons = darkIcons
      )

      with(CachedStatusBarConfig) {
        this.visible = visible
        this.backgroundColor = backgroundColor
        this.darkIcons = darkIcons
      }
    }
  }
}

private object CachedStatusBarConfig {
  var visible = true
  var backgroundColor = Color.Transparent
  var darkIcons = false
}

var statusBarLocked by mutableStateOf(false)