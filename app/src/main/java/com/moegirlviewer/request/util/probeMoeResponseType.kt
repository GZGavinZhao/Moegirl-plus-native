package com.moegirlviewer.request.util

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.moegirlviewer.request.MoeRequestException
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup

fun Response.probeMoeResponseType(bodyContent: String): MoeResponseType {
  if (this.request.url.queryParameter("rs") == "AJAXPoll::submitVote") return MoeResponseType.POLL

  return try {
    val jsonObject = JsonParser.parseString(bodyContent).asJsonObject
    if (jsonObject.has("error"))
      MoeResponseType.ERROR else
      MoeResponseType.DATA
  } catch (e: JsonSyntaxException) {
    val htmlDoc = Jsoup.parse(bodyContent)
    when(htmlDoc.title()) {
      "AccessDeny" -> MoeResponseType.TX_BLOCKED
      "" -> MoeResponseType.TX_CAPTCHA
      else -> MoeResponseType.TX_CAPTCHA
    }
  }
}

fun ResponseBody.toMoeRequestError(bodyContent: String): MoeRequestException {
  val errJsonObject = JsonParser.parseString(bodyContent).asJsonObject
      .get("error").asJsonObject
  return MoeRequestException(
    code = errJsonObject.get("code").asString,
    message = errJsonObject.get("info").asString
  )
}

enum class MoeResponseType {
  DATA,
  ERROR,
  TX_CAPTCHA,
  TX_BLOCKED,
  POLL,
  UNKNOWN
}