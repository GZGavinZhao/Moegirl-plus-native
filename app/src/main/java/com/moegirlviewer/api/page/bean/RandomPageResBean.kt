package com.moegirlviewer.api.page.bean

data class RandomPageResBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val limits: Limits,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val grncontinue: String
  )

  data class Limits(
    val extracts: Int
  )

  data class Query(
    val pages: Map<Int,MapValue>
  ) {
    data class MapValue(
      val extract: String,
      val ns: Int,
      val pageid: Int,
      val pageimage: String,
      val thumbnail: Thumbnail? = null,
      val title: String
    ) {
      data class Thumbnail(
        val height: Int,
        val source: String,
        val width: Int
      )
    }
  }
}