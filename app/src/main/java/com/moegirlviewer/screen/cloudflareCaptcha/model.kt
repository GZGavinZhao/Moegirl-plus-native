package com.moegirlviewer.screen.cloudflareCaptcha

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.request.cookieJar
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
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
    val tokenCookieStr = cookieStr?.split(";")
      ?.map { it.trim() }
      ?.firstOrNull { it.contains(Regex("""^cf_clearance=""")) }

    if (tokenCookieStr == null) {
      webview.reload()
      return
    }

    val cookie = Cookie.Builder()
      .domain("hmoegirl.com")
      .name("cf_clearance")
      .value(tokenCookieStr.replace("cf_clearance=", ""))
      .expiresAt(253402300799999L)
      .build()
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