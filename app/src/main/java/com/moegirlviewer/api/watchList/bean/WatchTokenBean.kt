package com.moegirlviewer.api.watchList.bean

data class WatchTokenBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val tokens: Tokens
  ) {
    data class Tokens(
      val watchtoken: String
    )
  }
}