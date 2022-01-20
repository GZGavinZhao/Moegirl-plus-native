package com.moegirlviewer.api.account.bean

data class LoginTokenBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val tokens: Tokens
  ) {
    data class Tokens(
      val logintoken: String
    )
  }
}