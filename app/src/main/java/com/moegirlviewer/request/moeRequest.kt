package com.moegirlviewer.request

import android.util.Log
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.request.util.bodyContentHandle
import com.moegirlviewer.request.util.toQueryStringParams
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import java.net.SocketTimeoutException

val moeOkHttpClient = OkHttpClient.Builder()
  .cookieJar(cookieJar)
  .addInterceptor(CommonConfigInterceptor())
  .addInterceptor(MoeInterceptor())
  .addInterceptor(HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.BASIC
  })
  .build()

suspend fun <T> moeRequest(
  params: Map<String, Any> = emptyMap(),
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

  val response = try {
    moeOkHttpClient.newCall(request).execute()
  } catch (e: SocketTimeoutException) {
    throw MoeRequestTimeoutException(e)
  } catch (e: Exception) {
    throw MoeRequestTimeoutException(
      cause = e
    )
  }

  if (!response.isSuccessful) {
    if (response.code == 404) {
      throw MoeRequestTimeoutException()
    } else {
      val bodyContent = response.body?.string() ?: ""

      if (bodyContent.contains("Cloudflare", true)) {
        withContext(Dispatchers.Main) {
          Globals.navController.navigate("cloudflareCaptcha") {
            this.launchSingleTop = true
          }
        }

//        toast(Globals.context.getString(R.string.blockedByCloudflareHint))
        throw MoeRequestWikiException("被Cloudflare captcha拦截")
      } else {
        Log.e("[MoeRequestHttpException]", bodyContent ?: "body不存在")
        throw MoeRequestHttpException(
          code = response.code,
          message = response.message
        )
      }
    }
  }

  response.body!!.string().bodyContentHandle(entity)
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
  message = message,
  code = code,
  cause = cause
)

open class MoeRequestHttpException(
  message: String,
  code: Int = 0,
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
