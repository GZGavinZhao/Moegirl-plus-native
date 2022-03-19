package com.moegirlviewer.api.category.bean

data class PageCategoriesBean(
  val batchcomplete: String,
  val query: Query
) {
  data class Query(
    val pages: Map<Int,MapValue>
  ) {
    data class MapValue(
      val categories: List<Category>? = null,
      val ns: Int,
      val pageid: Int,
      val title: String
    ) {
      data class Category(
        val ns: Int,
        val title: String
      )
    }
  }
}