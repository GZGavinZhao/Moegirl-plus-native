package com.moegirlviewer.request

import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.util.Globals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

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

suspend fun <T> Request.send(entity: Class<T>): T {
  return withContext(Dispatchers.IO) {
    val res = try {
      commonOkHttpClient.newCall(this@send).execute()
    } catch (e: Exception) {
      throw CommonRequestException(
        message = e.message,
        cause = e,
      )
    }

    if (res.isSuccessful) {
      Gson().fromJson(res.body!!.string(), entity)
    } else {
      throw CommonRequestException(res.message)
    }
  }
}