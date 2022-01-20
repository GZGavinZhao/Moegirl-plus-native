package com.moegirlviewer.api.editingRecord.bean

data class PageRevisionsBean(
  val `continue`: Continue? = null,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val rvcontinue: String
  )

  data class Query(
    val pages: Map<Int,MapValue>
  ) {
    data class MapValue(
      val ns: Int,
      val pageid: Int,
      val revisions: List<Revision>?,
      val title: String,
      val missing: String? = null,
    ) {
      data class Revision(
        val comment: String,
        val minor: String,
        val parentid: Int,
        val revid: Int,
        val size: Int,
        val timestamp: String,
        val user: String
      )
    }
  }
}