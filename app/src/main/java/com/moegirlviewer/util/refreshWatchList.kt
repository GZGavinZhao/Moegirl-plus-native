package com.moegirlviewer.util

import com.moegirlviewer.api.watchList.WatchListApi
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.room.watchingPage.WatchingPage

suspend fun refreshWatchList(): Boolean {
  var continueKey: String? = null
  var retryFlag = 0 // 0~3：失败重试第0次到第三次；4：失败4次，退出循环；5：全部加载完成
  val resultList = mutableListOf<String>()

  suspend fun loadList() {
    val res = WatchListApi.getRawWatchList(continueKey)
    continueKey = res.`continue`?.wrcontinue
    resultList.addAll(res.watchlistraw.map { it.title })
  }

  while(retryFlag < 4) {
    try {
      if (listOf(1, 2, 3).contains(retryFlag)) println("加载原始监视列表失败，重试第${retryFlag}次")
      loadList()
      retryFlag = 0
      if (continueKey == null) retryFlag = 5
    } catch (e: MoeRequestException) {
      retryFlag++
      printRequestErr(e)
    }
  }

  return if (retryFlag == 5) {
    Globals.room.watchingPage().clear()
    Globals.room.watchingPage().insertItem(
      *resultList.map { WatchingPage(it) }.toTypedArray()
    )
    true
  } else {
    false
  }
}