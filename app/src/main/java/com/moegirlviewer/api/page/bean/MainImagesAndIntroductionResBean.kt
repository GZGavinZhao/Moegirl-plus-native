package com.moegirlviewer.api.page.bean

data class MainImagesAndIntroductionResBean(
  val batchcomplete: String,
  val limits: Limits,
  val query: Query
) {
  data class Limits(
    val extracts: Int
  )

  data class Query(
    val pages: Map<Int, MapValue>
  ) {
    data class MapValue(
      val extract: String,
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