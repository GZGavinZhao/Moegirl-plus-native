package com.moegirlviewer.api.page.bean

data class PageProfileBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val pages: Map<Int, MapValue>
  ) {
    data class MapValue(
      val extract: String,
      val ns: Int,
      val pageid: Int,
      val pageimage: String,
      val thumbnail: Thumbnail?,
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