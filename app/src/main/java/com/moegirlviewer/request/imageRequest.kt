package com.moegirlviewer.request

import okhttp3.OkHttpClient

val imageOkHttpClient = OkHttpClient.Builder()
  .cookieJar(cookieJar)
  .addInterceptor(CommonConfigInterceptor())
  .build()