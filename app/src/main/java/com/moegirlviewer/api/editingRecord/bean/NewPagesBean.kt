package com.moegirlviewer.api.editingRecord.bean

data class NewPagesBean(
  val batchcomplete: String,
  val `continue`: Continue? = null,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val rccontinue: String
  )

  data class Query(
    val recentchanges: List<Recentchange>
  ) {
    data class Recentchange(
      val ns: Int,
      val old_revid: Int,
      val pageid: Int,
      val rcid: Int,
      val revid: Int,
      val timestamp: String,
      val title: String,
      val type: String
    )
  }
}

//data class NewPagesBean(
//  val batchcomplete: String,
//  val `continue`: Continue,
//  val query: Query
//) {
//  data class Continue(
//    val `continue`: String,
//    val grccontinue: String
//  )
//
//  data class Query(
//    val pages: Map<Int,MapValue>
//  ) {
//    data class MapValue(
//      val extract: String,
//      val ns: Int,
//      val pageid: Int,
//      val thumbnail: Thumbnail? = null,
//      val title: String,
//    ) {
//      data class Thumbnail(
//        val height: Int,
//        val source: String,
//        val width: Int
//      )
//    }
//  }
//}