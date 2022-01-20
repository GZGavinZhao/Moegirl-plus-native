package com.moegirlviewer.api.notification.bean

data class MarkAllAsReadResultBean(
  val query: Query
) {
  data class Query(
    val echomarkread: Echomarkread
  ) {
    data class Echomarkread(
      val alert: Alert,
      val count: String,
      val message: Message,
      val rawcount: Int,
      val result: String
    ) {
      data class Alert(
        val count: String,
        val rawcount: Int
      )

      data class Message(
        val count: String,
        val rawcount: Int
      )
    }
  }
}