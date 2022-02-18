package com.moegirlviewer.screen.cloudflareCaptcha

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.screen.home.HomeScreenModel
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.HmoeSplashImageManager
import com.moegirlviewer.util.globalCoroutineScope
import com.moegirlviewer.util.toast
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CloudflareCaptchaScreen() {
  val model: CloudflareCaptchaScreenModel = hiltViewModel()

  LaunchedEffect(true) {
    toast(Globals.context.getString(R.string.cloudflareValidationHint))
  }

  LaunchedEffect(true) {
    while (true) {
      delay(300)
      println(model.webview.title)
      if (model.webview.title == "H萌娘") model.extractCloudflareToken()
    }
  }

  DisposableEffect(true) {
    onDispose {
      HomeScreenModel.needReload = true
      globalCoroutineScope.launch {
        HmoeSplashImageManager.syncImagesByConfig()
      }
    }
  }

  BackHandler {
    toast(Globals.context.getString(R.string.cloudflareValidationHint))
  }

  StatusBar(
    darkIcons = true
  )

  AndroidView(
    factory = {
      WebView(it).apply {
        model.webview = this
        settings.javaScriptEnabled = true
        settings.setSupportMultipleWindows(true)
        webViewClient = object : WebViewClient() {
          override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view!!.loadUrl(request!!.url.toString())
            return false
          }
        }

        loadUrl(Constants.mainPageUrl)
      }
    }
  )
}