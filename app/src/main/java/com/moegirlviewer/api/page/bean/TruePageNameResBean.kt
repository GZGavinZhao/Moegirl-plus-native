package com.moegirlviewer.api.page.bean

data class TruePageNameResBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val pages: Map<Int, MapValue>
  ) {
    data class MapValue(
      val ns: Int,
      val pageid: Int,
      val title: String
    )
  }
}