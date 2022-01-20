package com.moegirlviewer.api.account.bean

data class LoginResultBean(
  val clientlogin: Clientlogin
) {
  data class Clientlogin(
    val status: String,
    val username: String? = null,
    val message: String? = null,
    val messagecode: String? = null,
  )
}