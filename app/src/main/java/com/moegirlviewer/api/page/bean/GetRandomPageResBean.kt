package com.moegirlviewer.api.page.bean

data class GetRandomPageResBean(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val rncontinue: String
  )

  data class Query(
    val random: List<Random>
  ) {
    data class Random(
      val id: Int,
      val ns: Int,
      val title: String
    )
  }
}