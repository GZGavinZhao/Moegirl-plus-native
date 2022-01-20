package com.moegirlviewer.request

import okhttp3.OkHttpClient

val commonOkHttpClient = OkHttpClient.Builder()
  .addInterceptor(CommonConfigInterceptor())
  .build()