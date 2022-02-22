package com.moegirlviewer.api.search.bean

data class SearchResultBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val gsroffset: Int,
    val sroffset: Int
  )

  data class Query(
    val pages: Map<Int,MapValue>,
    val search: List<Search>,
    val searchinfo: Searchinfo
  ) {
    data class MapValue(
      val index: Int,
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

    open class Search(
      val categorysnippet: String? = null,
      val redirecttitle: String? = null,
      val sectiontitle: String? = null,
      val ns: Int,
      val pageid: Int,
      val snippet: String,
      val timestamp: String,
      val title: String
    )

    data class Searchinfo(
      val totalhits: Int
    )
  }
}