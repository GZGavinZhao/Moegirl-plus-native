package com.moegirlviewer.request

import android.util.Log
import com.google.gson.Gson
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.request.util.MoeResponseType.*
import com.moegirlviewer.request.util.probeMoeResponseType
import com.moegirlviewer.request.util.toMoeRequestError
import com.moegirlviewer.request.util.toQueryStringParams
import com.moegirlviewer.screen.captcha.CaptchaRouteArguments
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.net.SocketTimeoutException

val moeOkHttpClient = OkHttpClient.Builder()
  .cookieJar(cookieJar)
  .addInterceptor(CommonConfigInterceptor())
  .addInterceptor(MoeInterceptor())
  .addInterceptor(HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.BODY
  })
  .build()

suspend fun <T> moeRequest(
  params: Map<String, Any>,
  method: MoeRequestMethod = MoeRequestMethod.GET,
  baseUrl: String = Constants.apiUrl,
  entity: Class<T>? = null,
): T = withContext(Dispatchers.IO) {
  val urlBuilder = baseUrl.toHttpUrlOrNull()!!.newBuilder()
  val requestBuilder = Request.Builder()

  when (method) {
    MoeRequestMethod.GET -> {
      for ((key, value) in params) {
        when(value) {
         is List<*> -> {
           value.map { it.toString() }.toQueryStringParams(key).forEach {
             urlBuilder.addQueryParameter(it.first, it.second)
           }
         }
          else -> urlBuilder.addQueryParameter(key, value.toString())
        }
      }
    }
    MoeRequestMethod.POST -> {
      val bodyBuilder = FormBody.Builder()
      for ((key, value) in params) bodyBuilder.add(key, value.toString())
      requestBuilder.post(bodyBuilder.build())
    }
  }

  val request = requestBuilder
    .url(urlBuilder.build())
    .build()

  try {
    val response = moeOkHttpClient.newCall(request).execute()

    if (!response.isSuccessful) {
      if (response.code == 404) {
        throw MoeRequestTimeoutException()
      } else {
        Log.e("[MoeRequestHttpException]", response.body?.string() ?: "body不存在")
        throw MoeRequestHttpException(
          code = response.code,
          message = response.message
        )
      }
    }

    val bodyContent = response.body!!.string()

    when(response.probeMoeResponseType(bodyContent)) {
      DATA -> {
        Gson().fromJson(bodyContent, entity)
      }

      ERROR -> {
        throw response.body!!.toMoeRequestError(bodyContent)
      }

      POLL -> {
        bodyContent as T
      }

      TX_CAPTCHA -> {
        withContext(Dispatchers.Main) {
          Globals.navController.navigate(CaptchaRouteArguments(
            captchaHtml = bodyContent,
          )) {
            this.launchSingleTop = true
          }
        }

        throw MoeRequestWikiException("被腾讯captcha拦截")
      }

      TX_BLOCKED -> {
        toast(Globals.context.getString(R.string.txBlocked))
        throw MoeRequestWikiException("被腾讯防火墙拦截")
      }

      UNKNOWN -> {
        throw MoeRequestWikiException("未知错误")
      }
    }
  } catch (e: SocketTimeoutException) {
    throw MoeRequestTimeoutException(e)
  }
}

enum class MoeRequestMethod {
  GET,
  POST
}

open class MoeRequestException(
  override val message: String,
  val code: String,
  cause: Throwable? = null,
) : Exception(message, cause)

open class MoeRequestWikiException(
  message: String,
  code: String = "customError",
  cause: Throwable? = null
) : MoeRequestException(
  message = "[MoeRequestWikiException] $code: $message",
  code = code,
  cause = cause
)

open class MoeRequestHttpException(
  message: String,
  code: Int,
  cause: Throwable? = null
) : MoeRequestException(
  message = message,
  code = "http:$code",
  cause = cause
)

class MoeRequestTimeoutException(
  cause: Throwable? = null
) : MoeRequestHttpException(
  message = Globals.context.getString(R.string.netErr),
  code = 404,
  cause = cause
)