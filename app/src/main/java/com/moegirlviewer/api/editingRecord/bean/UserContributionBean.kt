package com.moegirlviewer.api.editingRecord.bean

data class UserContributionBean(
  val batchcomplete: String,
  val `continue`: Continue?,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val uccontinue: String
  )

  data class Query(
    val usercontribs: List<Usercontrib>
  ) {
    data class Usercontrib(
      val comment: String,
      val minor: String,
      val ns: Int,
      val pageid: Int,
      val parentid: Int,
      val revid: Int,
      val sizediff: Int,
      val tags: List<String>,
      val timestamp: String,
      val title: String,
      val top: String,
      val user: String,
      val userid: Int
    )
  }
}