package com.moegirlviewer.screen.cloudflareCaptcha

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.htmlWebView.HtmlWebViewContent
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.request.cookieJar
import com.moegirlviewer.screen.captcha.CaptchaRouteArguments
import com.moegirlviewer.screen.home.HomeScreenModel
import com.moegirlviewer.util.CachedWebViews
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class CloudflareCaptchaScreenModel @Inject constructor() : ViewModel() {
  lateinit var webview: WebView
  val cookieDomain = "https://hmoegirl.com"
  private var isExtractCloudflareTokenExecuted = false

  fun extractCloudflareToken() {
    if (isExtractCloudflareTokenExecuted) return
    val cookieManager = CookieManager.getInstance()
    val cookieStr: String? = cookieManager.getCookie(cookieDomain)
    val tokenCookieStr = cookieStr?.split(";")?.firstOrNull { it.contains(Regex("""^cf_clearance=""")) }

    if (tokenCookieStr == null) {
      webview.reload()
      return
    }

    val cookie = Cookie.parse(cookieDomain.toHttpUrl(), tokenCookieStr)!!
    cookieJar.saveFromResponse(cookieDomain.toHttpUrl(), listOf(cookie))
    toast(Globals.context.getString(R.string.complateValidate))
    Globals.navController.popBackStack()
    isExtractCloudflareTokenExecuted = true
  }

  override fun onCleared() {
    super.onCleared()
    webview.destroy()
  }
}