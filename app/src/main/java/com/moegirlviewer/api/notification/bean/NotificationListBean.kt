package com.moegirlviewer.api.notification.bean

import com.google.gson.annotations.SerializedName

data class NotificationListBean(
  val batchcomplete: Boolean,
  val query: Query
) {
  data class Query(
    val notifications: Notifications
  ) {
    data class Notifications(
      val `continue`: String? = null,
      val list: List<Notification>,
      val rawcount: Int,
      val count: String
    ) {
      data class Notification(
        @SerializedName("*")
        val _asterisk: Initiator,
        val agent: Agent,
        val category: String,
        val id: String,
        val read: String? = null,
        val targetpages: List<Any>,
        val timestamp: Timestamp,
        val title: Title? = null,
        val type: String,
        val wiki: String
      ) {
        data class Initiator(
          val body: String,
          val compactHeader: String,
          val header: String,
          val icon: String,
          val iconUrl: String,
          // 这个links的primary属性，有时是数组有时是对象，现在暂时也用不到，先注释掉
//          val links: Links
        ) {
//          data class Links(
//            val primary: Primary,
//            val secondary: List<Secondary>
//          ) {
//            data class Primary(
//              val label: String,
//              val url: String
//            )
//
//            data class Secondary(
//              val description: String,
//              val icon: String,
//              val label: String,
//              val prioritized: Boolean,
//              val tooltip: String,
//              val url: String
//            )
//          }
        }

        data class Agent(
          val id: Int,
          val name: String
        )

        data class Timestamp(
          val date: String,
          val mw: String,
          val unix: String,
          val utciso8601: String,
          val utcmw: String,
          val utcunix: String
        )

        data class Title(
          val full: String,
          val namespace: String,
          @SerializedName("namespace-key")
          val namespaceKey: Int,
          val text: String
        )
      }
    }
  }
}