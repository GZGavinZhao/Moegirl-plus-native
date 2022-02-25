package com.moegirlviewer.api.search.bean

data class SearchHintBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val psoffset: Int
  )

  data class Query(
    val prefixsearch: List<Prefixsearch>
  ) {
    data class Prefixsearch(
      val ns: Int,
      val pageid: Int,
      val title: String
    )
  }
}