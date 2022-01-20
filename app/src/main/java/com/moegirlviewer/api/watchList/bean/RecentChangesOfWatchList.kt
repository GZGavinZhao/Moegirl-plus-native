package com.moegirlviewer.api.watchList.bean

data class RecentChangesOfWatchList(
  val batchcomplete: String,
  val `continue`: Continue,
  val query: Query
) {
  data class Continue(
    val `continue`: String,
    val wlcontinue: String
  )

  data class Query(
    val watchlist: List<Watchlist>
  ) {
    data class Watchlist(
      val comment: String,
      val minor: String,
      val newlen: Int,
      val ns: Int,
      val old_revid: Int,
      val oldlen: Int,
      val pageid: Int,
      val revid: Int,
      val timestamp: String,
      val title: String,
      val type: String,
      val user: String
    )
  }
}