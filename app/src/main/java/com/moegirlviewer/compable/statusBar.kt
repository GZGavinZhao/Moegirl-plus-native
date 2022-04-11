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
  val refStatusBarLocked = statusBarLocked   // state必须出现在composable函数的上下文中，才能正确触发组件重渲染

  LaunchedEffect(
    visible,
    backgroundColor,
    darkIcons,
    refStatusBarLocked
  ) {
    if (refStatusBarLocked) return@LaunchedEffect
    systemUiController.isStatusBarVisible = visible
    systemUiController.setStatusBarColor(
      color = backgroundColor,
      darkIcons = darkIcons
    )
  }
}

var statusBarLocked by mutableStateOf(false)