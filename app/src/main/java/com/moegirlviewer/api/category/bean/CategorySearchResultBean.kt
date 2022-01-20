package com.moegirlviewer.api.category.bean

data class CategorySearchResultBean(
  val batchcomplete: String,
  val `continue`: Continue?,
  val query: Query?
) {
  data class Continue(
    val `continue`: String,
    val gcmcontinue: String
  )

  data class Query(
    val pages: Map<Int,MapValue>
  ) {
    data class MapValue(
      val categories: List<Category>,
      val ns: Int,
      val pageid: Int,
      val pageimage: String,
      val thumbnail: Thumbnail? = null,
      val title: String
    ) {
      data class Category(
        val ns: Int,
        val title: String
      )

      data class Thumbnail(
        val height: Int,
        val source: String,
        val width: Int
      )
    }
  }
}