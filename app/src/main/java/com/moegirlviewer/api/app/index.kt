package com.moegirlviewer.api.app

import com.google.gson.Gson
import com.moegirlviewer.api.app.bean.AppLastVersionBean
import com.moegirlviewer.api.app.bean.HmoeSplashImageConfigBean
import com.moegirlviewer.api.app.bean.MoegirlSplashImageBean
import com.moegirlviewer.request.CommonRequestException
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.request.send
import com.moegirlviewer.util.isMoegirl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

val getMoegirlDescRegex = Regex("""## Moegirl\+[\r\n]+([\s\S]+?)([\r\n]+##|$)""")
val getHmoeDescRegex = Regex("""## HMoegirl[\r\n]+([\s\S]+)([\r\n]+##|$)""")

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
            desc = isMoegirl(getMoegirlDescRegex, getHmoeDescRegex).find(resBody.body)?.groupValues?.get(1),
            downloadUrl = resBody.assets.first { it.name == isMoegirl("Moegirl+.apk","HMoegirl.apk") }.browser_download_url
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

  suspend fun getMoegirlSplashImageConfig(): List<MoegirlSplashImageBean> {
    val request = Request.Builder()
      .url("https://mzh.moegirl.org.cn/index.php?curid=519007&action=raw")
      .build()
    return request.send(
      entity = Array<MoegirlSplashImageBean>::class.java,
    ).toList()
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
  val desc: String?,
  val downloadUrl: String
)