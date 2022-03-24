package com.moegirlviewer.util

import com.moegirlviewer.api.watchList.WatchListApi
import com.moegirlviewer.room.watchingPage.WatchingPage

suspend fun refreshWatchList() {
  val rawWatchList = WatchListApi.getRawWatchList()
  Globals.room.watchingPage().clear()
  Globals.room.watchingPage().insertItem(
    *rawWatchList.map { WatchingPage(it) }.toTypedArray()
  )
}