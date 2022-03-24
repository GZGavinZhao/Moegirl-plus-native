package com.moegirlviewer.util

import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.store.AccountStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

suspend fun initUserInfo() {
  initNecessaryUserInfo()
  initDeferrableUserInfo()
}

suspend fun initNecessaryUserInfo() {
  try {
    AccountStore.loadUserInfo()
  } catch (e: MoeRequestException) {
    printRequestErr(e, "初始化用户信息失败")
  }
}

suspend fun initDeferrableUserInfo() = coroutineScope {
  launch {
    try {
      refreshWatchList()
    } catch (e: MoeRequestException) {
      printPlainLog("刷新监视列表失败")
    }
  }

  launch {
    try {
      AccountStore.checkWaitingNotificationTotal()
    } catch (e: MoeRequestException) {
      printRequestErr(e, "检查用户等待通知失败")
    }
  }
}