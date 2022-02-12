package com.moegirlviewer.api.app

import com.google.gson.Gson
import com.moegirlviewer.api.app.bean.AppLastVersionBean
import com.moegirlviewer.api.app.bean.HmoeSplashImageConfigBean
import com.moegirlviewer.request.CommonRequestException
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.request.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

object AppApi {
  suspend fun getLastVersion(): AppLastVersion {
    try {
      val request = Request.Builder()
        .url("https://api.github.com/repos/koharubiyori/Moegirl-plus-native/releases/latest")
        .build()
      return withContext(Dispatchers.IO) {
        val res = commonOkHttpClient.newCall(request).execute()
        if (res.isSuccessful) {
          val resBody = Gson().fromJson(res.body!!.string(), AppLastVersionBean::class.java)
          AppLastVersion(
            version = resBody.tag_name,
            moegirlDesc = Regex("""## Moegirl\+\n([\s\S]+?)(\n##|$)""").find(resBody.body)?.groupValues?.get(1),
            hmoeDesc = Regex("""## HMoegirl\n([\s\S]+)(\n##|$)""").find(resBody.body)?.groupValues?.get(1)
          )
        } else {
          throw CommonRequestException(res.message)
        }
      }
    } catch (e: Exception) {
      throw CommonRequestException(
        cause = e
      )
    }
  }

  suspend fun getHmoeSplashImageConfig(): HmoeSplashImageConfigBean {
    val request = Request.Builder()
      .url("https://www.hmoegirl.com/index.php?title=User:%E6%9D%B1%E6%9D%B1%E5%90%9B/app/splashImages&action=raw")
      .build()
    return request.send(HmoeSplashImageConfigBean::class.java)
  }
}

class AppLastVersion(
  val version: String,
  val moegirlDesc: String?,
  val hmoeDesc: String?
)