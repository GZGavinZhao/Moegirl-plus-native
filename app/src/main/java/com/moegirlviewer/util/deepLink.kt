package com.moegirlviewer.util

import android.content.Intent
import androidx.compose.ui.unit.Constraints
import androidx.core.net.toUri
import com.moegirlviewer.Constants
import com.moegirlviewer.DataSource
import java.net.URLDecoder

private val plainNameRegex = if (Constants.source == DataSource.MOEGIRL)
  Regex("""moegirl\.org\.cn/(.+)$""") else
  Regex("""hmoegirl\.com/(.+)$""")
private val pageIdRegex = Regex("""curid=\d+""")

val Intent.deepLink get(): DeepLink? {
  val deepLinkStr = this.dataString ?: return null
  return when {
    deepLinkStr.contains(pageIdRegex) -> {
      val pageId = deepLinkStr.toUri().getQueryParameter("curid")!!.toInt()
      PageIdDeepLink(pageId)
    }
    deepLinkStr.contains(plainNameRegex) -> {
      val pageName = URLDecoder.decode(plainNameRegex.find(deepLinkStr)!!.groupValues[1].let { if (it == "") "mainpage" else it }, "utf-8")
      PageNameDeepLink(
        pageName = pageName,
        isMainPage = pageName.lowercase() == "mainpage"
      )
    }
    else -> null
  }
}

sealed class DeepLink

class PageNameDeepLink(
  val pageName: String,
  val isMainPage: Boolean
) : DeepLink()

class PageIdDeepLink(
  val pageId: Int,
) : DeepLink()