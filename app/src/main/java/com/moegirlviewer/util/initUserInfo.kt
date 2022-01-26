package com.moegirlviewer.util

import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.store.AccountStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun initUserInfo() {
  withContext(Dispatchers.IO) {
    launch {
      try {
        AccountStore.loadUserInfo()
      } catch (e: MoeRequestException) {
        printRequestErr(e, "初始化用户信息失败")
      }
    }

    launch {
      if (!refreshWatchList()) {
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
}