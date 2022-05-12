package com.moegirlviewer.compable

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.moegirlviewer.util.*

@Composable
fun StatusBar(
  mode: StatusBarMode = StatusBarMode.VISIBLE,
  sticky: Boolean = false,
  backgroundColor: Color = Color.Transparent,
  darkIcons: Boolean = false,
) {
  val systemUiController = rememberSystemUiController()
  val refStatusBarLocked = statusBarLocked   // state必须出现在composable函数的上下文中，才能正确触发组件重渲染

  fun syncConfig() {
    when(mode) {
      StatusBarMode.VISIBLE -> {
        Globals.activity.useFreeStatusBarLayout()
        systemUiController.isStatusBarVisible = true
      }
      StatusBarMode.HIDE -> systemUiController.isStatusBarVisible = false
      StatusBarMode.STICKY -> {
        Globals.activity.useStickyStatusBarLayout()
      }
    }
    systemUiController.setStatusBarColor(
      color = backgroundColor,
      darkIcons = darkIcons
    )

    with(CachedStatusBarConfig) {
      this.mode = mode
      this.sticky = sticky
      this.backgroundColor = backgroundColor
      this.darkIcons = darkIcons
    }
  }

  LaunchedEffect(
    mode,
    backgroundColor,
    darkIcons,
    refStatusBarLocked
  ) {
    if (refStatusBarLocked) return@LaunchedEffect
    syncConfig()
  }

  LifecycleEventEffect(
    onResume = {
      if (refStatusBarLocked) return@LifecycleEventEffect
      syncConfig()
    }
  )
}

var statusBarLocked by mutableStateOf(false)
object CachedStatusBarConfig {
  var mode = StatusBarMode.VISIBLE
  var sticky = false
  var backgroundColor = Color.Transparent
  var darkIcons = false
}

enum class StatusBarMode {
  VISIBLE,
  HIDE,
  STICKY
}