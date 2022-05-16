package com.moegirlviewer.api.search.bean

data class SearchHintBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val pages: Map<Int,MapValue>,
    val prefixsearch: List<Prefixsearch>
  ) {
    data class MapValue(
      val index: Int,
      val ns: Int,
      val pageid: Int,
      val pageimage: String,
      val thumbnail: Thumbnail?,
      val title: String,
      val extract: String
    ) {
      data class Thumbnail(
        val height: Int,
        val source: String,
        val width: Int
      )
    }

    data class Prefixsearch(
      val ns: Int,
      val pageid: Int,
      val title: String
    )
  }
}