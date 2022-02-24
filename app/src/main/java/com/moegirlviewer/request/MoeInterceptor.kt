package com.moegirlviewer.request

import com.moegirlviewer.util.isTraditionalChineseEnv
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

class MoeInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val newRequestBuilder = originalRequest.newBuilder()

    val extraParams = mapOf(
      "format" to "json",
      "variant" to if (isTraditionalChineseEnv()) "zh-hant" else "zh-hans"
    )

    if (originalRequest.method == MoeRequestMethod.GET.name) {
      val newUrlBuilder = originalRequest.url.newBuilder()
      for ((name, value) in extraParams) newUrlBuilder.addQueryParameter(name, value)
      newRequestBuilder.url(newUrlBuilder.build())
    } else {
      if (originalRequest.body is FormBody) {
        val newBodyBuilder = FormBody.Builder()
        for ((name, value) in extraParams) newBodyBuilder.add(name, value)
        val originalBody = originalRequest.body as FormBody

        for (i in 0 until originalBody.size) {
          val originalName = originalBody.name(i)
          val originalValue = originalBody.value(i)
          newBodyBuilder.add(originalName, originalValue)
        }

        newRequestBuilder.post(newBodyBuilder.build())
      }
    }

    return chain.proceed(newRequestBuilder.build())
  }
}