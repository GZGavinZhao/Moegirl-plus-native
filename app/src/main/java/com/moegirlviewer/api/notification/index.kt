package com.moegirlviewer.api.notification

import com.moegirlviewer.api.edit.EditApi
import com.moegirlviewer.api.notification.bean.MarkAllAsReadResultBean
import com.moegirlviewer.api.notification.bean.NotificationListBean
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.moeRequest

object NotificationApi {
  suspend fun getList(
    continueKey: String? = null,
    limit: Int = 50,
  ) = moeRequest(
    entity = NotificationListBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["meta"] = "notifications"
      this["notunreadfirst"] = "1"
      this["notalertunreadfirst"] = "1"
      this["formatversion"] = "2"
      this["notlimit"] = limit
      this["notprop"] = "list|count"
      this["notsections"] = "message|alert"
      this["notformat"] = "model"
      this["notcrosswikisummary"] = "1"
      if (continueKey != null) this["notcontinue"] = continueKey
    }
  )

  suspend fun markAllAsRead(): Boolean {
    val token = EditApi.getCsrfToken()
    val res = moeRequest(
      entity = MarkAllAsReadResultBean::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "echomarkread",
        "all" to 1,
        "token" to token
      )
    )

    return res.query.echomarkread.result == "success"
  }
}

