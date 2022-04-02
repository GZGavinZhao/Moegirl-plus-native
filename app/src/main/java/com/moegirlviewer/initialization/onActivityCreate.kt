package com.moegirlviewer.initialization

import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.isDebugEnv
import com.moegirlviewer.util.useFreeStatusBarLayout

fun ComponentActivity.initializeOnCreate() {
  WebView.setWebContentsDebuggingEnabled(isDebugEnv())
  WebView.enableSlowWholeDocumentDraw()

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    window.decorView.isForceDarkAllowed = false
  }

  val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
  val density = resources.displayMetrics.density
  val statusBarHeight = resources.getDimensionPixelSize(resourceId) / density

  Globals.statusBarHeight = statusBarHeight
}