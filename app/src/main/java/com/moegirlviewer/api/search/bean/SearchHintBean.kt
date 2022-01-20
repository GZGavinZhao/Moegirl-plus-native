package com.moegirlviewer.api.search.bean

data class SearchHintBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val sroffset: Int
  )

  data class Query(
    val search: List<Search>,
    val searchinfo: Searchinfo
  ) {
    data class Search(
      val ns: Int,
      val pageid: Int,
      val size: Int,
      val snippet: String,
      val timestamp: String,
      val title: String,
      val wordcount: Int
    )

    data class Searchinfo(
      val totalhits: Int
    )
  }
}