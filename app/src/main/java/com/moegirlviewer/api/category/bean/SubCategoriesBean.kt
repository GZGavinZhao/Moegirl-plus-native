package com.moegirlviewer.api.category.bean

data class SubCategoriesBean(
  val batchcomplete: String,
  val `continue`: Continue?,
  val query: Query
) {
  data class Continue(
    val cmcontinue: String,
    val `continue`: String
  )

  data class Query(
    val categorymembers: List<Categorymember>
  ) {
    data class Categorymember(
      val ns: Int,
      val title: String
    )
  }
}