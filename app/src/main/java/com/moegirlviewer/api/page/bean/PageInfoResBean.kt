package com.moegirlviewer.api.page.bean

data class PageInfoResBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val pages: Map<Int, MapValue>
  ) {
    data class MapValue(
      val contentmodel: String,
      val lastrevid: Int,
      val length: Int,
      val ns: Int,
      val pageid: Int,
      val pagelanguage: String,
      val pagelanguagedir: String,
      val pagelanguagehtmlcode: String,
      val protection: List<Protection>,
      val restrictiontypes: List<String>,
      val title: String,
      val touched: String,
      val watched: String? = null,
      val talkid: Int? = null
    ) {
      data class Protection(
        val type: String,
        val level: String
      )
    }
  }
}