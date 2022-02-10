package com.moegirlviewer.request.util

import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.request.MoeRequestWikiException
import com.moegirlviewer.screen.captcha.CaptchaRouteArguments
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response


internal suspend fun <T> Response.bodyContentHandle(
  entity: Class<T>?,
  unknownErrorHandle: (suspend (bodyContent: String?) -> T)? = null
): T {
  val bodyContent = body!!.string()

  return when(probeMoeResponseType(bodyContent)) {
    MoeResponseType.DATA -> {
      Gson().fromJson(bodyContent, entity)
    }

    MoeResponseType.ERROR -> {
      throw body!!.toMoeRequestError(bodyContent)
    }

    MoeResponseType.POLL -> {
      bodyContent as T
    }

    MoeResponseType.TX_CAPTCHA -> {
      withContext(Dispatchers.Main) {
        Globals.navController.navigate(
          CaptchaRouteArguments(
            captchaHtml = bodyContent,
          )
        ) {
          this.launchSingleTop = true
        }
      }

      throw MoeRequestWikiException("被腾讯captcha拦截")
    }

    MoeResponseType.TX_BLOCKED -> {
      toast(Globals.context.getString(R.string.txBlocked))
      throw MoeRequestWikiException("被腾讯防火墙拦截")
    }

    MoeResponseType.UNKNOWN -> {
      unknownErrorHandle?.invoke(bodyContent) ?: throw MoeRequestWikiException("未知错误")
    }
  }
}