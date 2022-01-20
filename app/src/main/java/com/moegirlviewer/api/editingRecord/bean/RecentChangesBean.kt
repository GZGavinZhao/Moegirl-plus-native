package com.moegirlviewer.api.editingRecord.bean

data class RecentChangesBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val rccontinue: String
  )

  data class Query(
    val recentchanges: List<Recentchange>
  ) {
    data class Recentchange(
      val comment: String,
      val minor: String? = null,
      val new: String? = null,
      val newlen: Int,
      val ns: Int,
      val old_revid: Int,
      val oldlen: Int,
      val pageid: Int,
      val rcid: Int,
      val redirect: String,
      val revid: Int,
      val tags: List<String>,
      val timestamp: String,
      val title: String,
      val type: String,
      val user: String
    )
  }
}