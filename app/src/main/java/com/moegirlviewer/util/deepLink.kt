package com.moegirlviewer.util

import android.content.Intent
import androidx.compose.ui.unit.Constraints
import androidx.core.net.toUri
import com.moegirlviewer.Constants
import com.moegirlviewer.DataSource
import io.ktor.http.*
import java.net.URL
import java.net.URLDecoder

private val plainNameRegex = if (Constants.source == DataSource.MOEGIRL)
  Regex("""moegirl\.org\.cn/(.+)$""") else
  Regex("""hmoegirl\.com/(.+)$""")
private val pageIdRegex = Regex("""curid=\d+""")

val Intent.deepLink get(): DeepLink? {
  val deepLinkStr = this.dataString?.let { URLDecoder.decode(it, "utf-8") } ?: return null
  return when {
    deepLinkStr.contains(pageIdRegex) -> {
      val pageId = deepLinkStr.toUri().getQueryParameter("curid")!!.toInt()
      PageIdDeepLink(pageId)
    }
    deepLinkStr.contains(plainNameRegex) -> {
      val pageName = plainNameRegex.find(deepLinkStr)!!.groupValues[1].let { if (it == "") "mainpage" else it }
      PageNameDeepLink(
        pageName = pageName,
        isMainPage = pageName.lowercase() == "mainpage"
      )
    }
    else -> {
      try {
        val pageName = deepLinkStr.toUri().getQueryParameter("title")
        PageNameDeepLink(
          pageName = pageName!!,
          isMainPage = pageName.lowercase() == "mainpage"
        )
      } catch (e: Exception) {
        printPlainLog("解析deepLink失败：${deepLinkStr}", e)
        null
      }
    }
  }
}

sealed class DeepLink

class PageNameDeepLink(
  val pageName: String,
  val isMainPage: Boolean = false
) : DeepLink()

class PageIdDeepLink(
  val pageId: Int,
) : DeepLink()