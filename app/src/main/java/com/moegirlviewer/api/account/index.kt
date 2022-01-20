package com.moegirlviewer.api.account

import com.moegirlviewer.api.account.bean.LoginResultBean
import com.moegirlviewer.api.account.bean.LoginTokenBean
import com.moegirlviewer.api.account.bean.UserInfoBean
import com.moegirlviewer.api.edit.EditApi
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.moeRequest

object AccountApi {
  suspend fun getLoginToken(): String {
    val res = moeRequest(
      entity = LoginTokenBean::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "query",
        "meta" to "tokens",
        "type" to "login"
      )
    )

    return res.query.tokens.logintoken
  }

  suspend fun login(userName: String, password: String): LoginResultBean {
    val token = getLoginToken()
    return login(token, userName, password)
  }

  suspend fun logout() {
    val csrfToken = EditApi.getCsrfToken()
    moeRequest(
      entity = Any::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "logout",
        "token" to csrfToken
      )
    )
  }

  suspend fun getInfo(): UserInfoBean {
    return moeRequest(
      entity = UserInfoBean::class.java,
      params = mapOf(
        "action" to "query",
        "meta" to "userinfo",
        "uiprop" to "groups|blockinfo"
      )
    )
  }

  suspend fun poll(pollId: String, answer: String, token: String): String {
    return moeRequest(
      entity = String::class.java,
      baseUrl = "https://zh.moegirl.org.cn/index.php",
      params = mutableMapOf<String, Any>().apply {
        this["action"] = "ajax"
        this["rs"] = "AJAXPoll::submitVote"
        this["rsargs"] = listOf(pollId, answer, token)
      }
    )
  }
}

private suspend fun login(token: String, userName: String, password: String) =
  moeRequest(
    entity = LoginResultBean::class.java,
    method = MoeRequestMethod.POST,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "clientlogin"
      this["loginmessageformat"] = "html"
      this["loginreturnurl"] = "https://zh.moegirl.org.cn/Mainpage"
      this["username"] = userName
      this["password"] = password
      this["rememberMe"] = true
      this["logintoken"] = token
    }
  )