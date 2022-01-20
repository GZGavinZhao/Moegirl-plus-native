package com.moegirlviewer.screen.captcha

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.htmlWebView.HtmlWebView

@Composable
fun CaptchaScreen(
  arguments: CaptchaRouteArguments
) {
  val model: CaptchaScreenModel = hiltViewModel()

  LaunchedEffect(true) {
    model.routeArguments = arguments
    model.initWebViewContent()
  }

  BackHandler { model.showExitHint() }

  StatusBar(
    darkIcons = false
  )

  model.memoryStore.Provider {
    model.cacheWebViews.Provider {
      HtmlWebView(
        baseUrl = "https://zh.moegirl.org.cn/Mainpage",
        messageHandlers = model.messageHandlers,
        ref = model.htmlWebViewRef
      )
    }
  }
}