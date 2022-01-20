package com.moegirlviewer.api.edit.bean

data class TimestampOfLastEdit(
  val `continue`: Continue,
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
      val revisions: List<Revision>? = null,
      val title: String
    ) {
      data class Revision(
        val timestamp: String
      )
    }
  }
}