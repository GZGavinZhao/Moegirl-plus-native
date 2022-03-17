package com.moegirlviewer.api.editingRecord.bean

data class NewPagesBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val grccontinue: String
  )

  data class Query(
    val pages: Map<Int,MapValue>
  ) {
    data class MapValue(
      val extract: String,
      val ns: Int,
      val pageid: Int,
      val thumbnail: Thumbnail? = null,
      val title: String,
    ) {
      data class Thumbnail(
        val height: Int,
        val source: String,
        val width: Int
      )
    }
  }
}