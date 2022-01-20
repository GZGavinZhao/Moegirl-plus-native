package com.moegirlviewer.initialization

import android.app.Activity
import android.view.View
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.isDebugEnv

fun ComponentActivity.initializeOnCreate() {
  WebView.setWebContentsDebuggingEnabled(isDebugEnv())
  WebView.enableSlowWholeDocumentDraw()

  // 删除默认的顶部状态栏高度偏移
  window.decorView.systemUiVisibility =
    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
      View.SYSTEM_UI_FLAG_LAYOUT_STABLE

  val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
  val scale = resources.displayMetrics.density
  val statusBarHeight = resources.getDimensionPixelSize(resourceId) / scale

  Globals.statusBarHeight = statusBarHeight
  Globals.activity = this
}