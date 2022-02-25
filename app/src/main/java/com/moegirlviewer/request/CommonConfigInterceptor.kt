package com.moegirlviewer.request

import com.moegirlviewer.Constants
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.isMoegirl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CommonConfigInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val newRequest = chain
      .withConnectTimeout(10, TimeUnit.SECONDS)
      .withReadTimeout(10, TimeUnit.SECONDS)
      .withWriteTimeout(10, TimeUnit.SECONDS)
      .request().newBuilder()
      .addHeader("cache-control", "no-cache")
      .addHeader("user-agent", isMoegirl(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.95 Safari/537.36",
        Globals.httpUserAgent
      ))
      .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\"")
      .addHeader("sec-ch-ua-mobile", "?0")
      .addHeader("sec-fetch-dest", "empty")
      .addHeader("sec-fetch-mode", "cors")
      .addHeader("sec-fetch-site", "same-origin")
      .addHeader("x-requested-with", "XMLHttpRequest")
      .addHeader("dnt", "1")
      .addHeader("pragma", "no-cache")
      .addHeader("accept", "*/*")
      .addHeader("Referer", Constants.mainUrl)
      .build()

    return chain.proceed(newRequest)
  }
}
