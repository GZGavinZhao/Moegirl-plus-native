package com.moegirlviewer.util

import android.content.Intent
import androidx.core.net.toUri

private val plainNameRegex = Regex(""".+/(.+)$""")
private val pageIdRegex = Regex("""curid=\d+""")

val Intent.deepLink get(): DeepLink? {
  val deepLinkStr = this.dataString ?: return null
  return when {
    deepLinkStr.contains(pageIdRegex) -> {
      val pageId = deepLinkStr.toUri().getQueryParameter("curid")!!.toInt()
      PageIdDeepLink(pageId)
    }
    deepLinkStr.contains(plainNameRegex) -> {
      val pageName = plainNameRegex.find(deepLinkStr)!!.groupValues[1]
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