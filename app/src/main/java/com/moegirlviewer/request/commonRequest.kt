package com.moegirlviewer.request

import com.moegirlviewer.R
import com.moegirlviewer.util.Globals
import okhttp3.OkHttpClient

val commonOkHttpClient = OkHttpClient.Builder()
  .addInterceptor(CommonConfigInterceptor())
  .build()

class CommonRequestException(
  message: String? = Globals.context.getString(R.string.netErr),
  code: String = "commonRequestError",
  cause: Throwable? = null,
) : MoeRequestException(
  message = "[CommonRequestException] $code: $message",
  code = code,
  cause = cause
)