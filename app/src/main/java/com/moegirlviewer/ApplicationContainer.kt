package com.moegirlviewer

import android.app.Application
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import com.moegirlviewer.store.AccountStore
import dagger.hilt.android.HiltAndroidApp
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import refreshWatchList
import java.util.*


@HiltAndroidApp
class ApplicationContainer : Application() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)

  override fun onCreate() {
    super.onCreate()
    initialize()
    registerTasks()
  }

  private fun initialize() {
    Globals.context = applicationContext
    Globals.room = initRoom(applicationContext)
    BigImageViewer.initialize(FrescoImageLoader.with(applicationContext))

    coroutineScope.launch {
      if (AccountStore.isLoggedIn.first()) {
        val isValidLogin = AccountStore.checkAccount()
        if (isValidLogin) {
          initUserInfo()
        } else {
          AccountStore.logout()
          toast(Globals.context.getString(R.string.invalidLoginStatusHint))
        }
      }
    }
  }

  private fun registerTasks() {
    registerTask(30_1000) {
      coroutineScope.launch {
        try {
          AccountStore.checkWaitingNotificationTotal()
        } catch (e: Exception) {
          printRequestErr(e, "检查用户等待通知失败")
        }
      }
    }
  }
}

private fun registerTask(period: Long, delay: Long = period, execute: () -> Unit): Timer {
  val taskTimer = Timer(true)
  taskTimer.schedule(object : TimerTask() {
    override fun run() { execute() }
  }, delay, period)

  return taskTimer
}