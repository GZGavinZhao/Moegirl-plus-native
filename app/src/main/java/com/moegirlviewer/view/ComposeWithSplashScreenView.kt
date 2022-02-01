package com.moegirlviewer.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import com.moegirlviewer.screen.splash.SplashScreen
import com.moegirlviewer.screen.splash.SplashScreenState
import com.moegirlviewer.util.SplashImage

@SuppressLint("CustomSplashScreen", "ViewConstructor")
class ComposeWithSplashScreenView(
  context: Context,
  splashImage: SplashImage
) : FrameLayout(context) {
  private val splashScreenView = SplashScreenView(context, splashImage)
  private val composeContentView = ComposeView(context)
  private var splashHid = false

  init {
    composeContentView.apply {
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
      )
    }

    addView(composeContentView)
    addView(splashScreenView)
    composeContentView.alpha = 0f
  }

  suspend fun appearSplashScreen() {
    splashScreenView.splashScreenState.showAppearAnimation()
  }

  suspend fun hideSplashScreen() {
    if (splashHid) return
    composeContentView.alpha = 1f
    splashScreenView.splashScreenState.showTransparentAnimation()
    this.removeView(splashScreenView)
    splashHid = true
  }

  fun setContent(content: @Composable () -> Unit) {
    composeContentView.setContent(content)
  }
}

@SuppressLint("CustomSplashScreen", "ViewConstructor")
class SplashScreenView(
  context: Context,
  private val splashImage: SplashImage
) : FrameLayout(context) {
  private val composeView = ComposeView(context)
  lateinit var splashScreenState: SplashScreenState

  init {
    fillContent()
    addView(composeView)
  }

  private fun fillContent() {
    composeView.setContent {
      val coroutineScope = rememberCoroutineScope()
      splashScreenState = remember { SplashScreenState(coroutineScope, splashImage) }

      SplashScreen(splashScreenState)
    }
  }
}