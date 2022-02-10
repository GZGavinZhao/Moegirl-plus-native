package com.moegirlviewer.api.page.bean

data class ImageInfoResBean(
  val batchcomplete: String,
  val query: Query?
) {
  data class Query(
    val pages: Map<Int, MapValue>
  ) {
    data class MapValue(
      val imageinfo: List<Imageinfo>?,
      val imagerepository: String,
      val known: String,
      val missing: String,
      val ns: Int,
      val title: String
    ) {
      data class Imageinfo(
        val descriptionshorturl: String,
        val descriptionurl: String,
        val url: String
      )
    }
  }
}