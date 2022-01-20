package com.moegirlviewer.api.search.bean

data class SearchResultBean(
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
    val searchinfo: SearchHintBean.Query.Searchinfo
  ) {
    data class Search(
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