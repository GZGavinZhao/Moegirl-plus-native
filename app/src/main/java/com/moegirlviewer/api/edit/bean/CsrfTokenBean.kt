package com.moegirlviewer.api.edit.bean

data class CsrfTokenBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val tokens: Tokens
  ) {
    data class Tokens(
      val csrftoken: String
    )
  }
}