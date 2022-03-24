package com.moegirlviewer.request.util

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.moegirlviewer.request.MoeRequestWikiException
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup

internal fun probeMoeResponseType(bodyContent: String): MoeResponseType {
  return try {
    val jsonObject = JsonParser.parseString(bodyContent).asJsonObject
    if (jsonObject.has("error"))
      MoeResponseType.ERROR else
      MoeResponseType.DATA
  } catch (e: JsonSyntaxException) {
    try {
      val htmlDoc = Jsoup.parse(bodyContent)
      when {
        htmlDoc.selectFirst("meta[name=\"keywords\"]")?.attr("content")?.contains("错误") == true -> MoeResponseType.TX_BLOCKED
        htmlDoc.title() == "" -> MoeResponseType.TX_CAPTCHA
        else -> MoeResponseType.UNKNOWN
      }
    } catch (e: Exception) {
      MoeResponseType.UNKNOWN
    }
  }
}

fun String.toMoeRequestError(): MoeRequestWikiException {
  val errJsonObject = JsonParser.parseString(this).asJsonObject
      .get("error").asJsonObject
  return MoeRequestWikiException(
    code = errJsonObject.get("code").asString,
    message = errJsonObject.get("info").asString
  )
}

enum class MoeResponseType {
  DATA,
  ERROR,
  TX_CAPTCHA,
  TX_BLOCKED,
  UNKNOWN
}