package com.moegirlviewer.api.page.bean

data class MainImagesResBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val pages: Map<Int, MapValue>
  ) {
    data class MapValue(
      val ns: Int,
      val pageid: Int,
      val pageimage: String? = null,
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