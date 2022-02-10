package com.moegirlviewer.screen.cloudflareCaptcha

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.toast
import kotlinx.coroutines.delay

// 没调试出来，虽然cookie取到了，但还是会被拦截，cloudflare会检查userAgentString，如果用的是桌面端的也不行
// 另外有些梯子的节点，无论怎么验证都过不去
// 未来尝试将请求器的header改为模拟手机浏览器的请求，目前模拟的是windows chrome浏览器的请求
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
//        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.95 Safari/537.36"
        settings.setSupportMultipleWindows(true)
        webViewClient = object : WebViewClient() {
          override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view!!.loadUrl(request!!.url.toString())
            return false
          }
        }

        loadUrl(model.cookieDomain)
      }
    }
  )
}