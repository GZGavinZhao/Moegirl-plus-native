package com.moegirlviewer.api.page.bean

data class PurgePageResultBean(
  val batchcomplete: String,
  val purge: List<Purge>
) {
  data class Purge(
    val ns: Int,
    val purged: String,
    val title: String
  )
}