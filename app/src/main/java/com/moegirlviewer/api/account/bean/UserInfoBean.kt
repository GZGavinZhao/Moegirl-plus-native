package com.moegirlviewer.api.account.bean

data class UserInfoBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val userinfo: Userinfo,
  ) {
    data class Userinfo(
      val groups: List<String>,
      val id: Int,
      val name: String,
      val anon: Any? = null
    )
  }
}