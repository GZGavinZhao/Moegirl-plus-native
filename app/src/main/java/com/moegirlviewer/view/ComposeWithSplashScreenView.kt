package com.moegirlviewer.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.screen.splash.SplashScreen
import com.moegirlviewer.screen.splash.SplashScreenState
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen", "ViewConstructor")
class ComposeWithSplashScreenView(
  context: Context,
  // 为了保证splashScreen在第一时间显示出来，必须在初始化时就把view加上，实测如果在要显示时才添加的话会导致慢一些
  initialAddSplashView: Boolean = true
) : FrameLayout(context) {
  private val splashScreenView = SplashScreenView(context)
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
    if (initialAddSplashView) addView(splashScreenView)
  }

  suspend fun appearSplashScreen() {
    splashScreenView.splashScreenState.showAppearAnimation()
  }

  suspend fun hideSplashScreen() {
    if (splashHid) return
    splashScreenView.splashScreenState.showTransparentAnimation()
    this.removeView(splashScreenView)
    splashHid = true
  }

  fun setContent(content: @Composable () -> Unit) {
    composeContentView.setContent(content)
  }
}

@SuppressLint("CustomSplashScreen")
class SplashScreenView(context: Context) : FrameLayout(context) {
  private val composeView = ComposeView(context)
  lateinit var splashScreenState: SplashScreenState

  init {
    fillContent()
    addView(composeView)
  }

  private fun fillContent() {
    composeView.setContent {
      val coroutineScope = rememberCoroutineScope()
      splashScreenState = remember { SplashScreenState(coroutineScope) }

      SplashScreen(splashScreenState)
    }
  }
}