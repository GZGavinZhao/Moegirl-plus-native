package com.moegirlviewer.api.watchList.bean

data class RawWatchListBean(
  val batchcomplete: String,
  val `continue`: Continue?,
  val watchlistraw: List<Watchlistraw>
) {
  data class Continue(
    val `continue`: String,
    val wrcontinue: String
  )

  data class Watchlistraw(
    val ns: Int,
    val title: String
  )
}