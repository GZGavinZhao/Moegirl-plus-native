package com.moegirlviewer.initialization

import android.util.Log
import androidx.compose.material.Text
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

private val coroutineScope = CoroutineScope(Dispatchers.Main)

fun onComposeCreated() {
  coroutineScope.launch { initAccount() }
  coroutineScope.launch { checkNewVersion() }
  registerTasks()
}

private suspend fun initAccount() {
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

private suspend fun checkNewVersion() = coroutineScope {
  val res = AppApi.getLastVersion()
  val currentVersion = Globals.context.packageManager.getPackageInfo(Globals.context.packageName, 0).versionName
  val rejectedVersion = SettingsStore.otherSettings.getValue { this.rejectedVersionName }.first()

  if (res.version != currentVersion && res.version != rejectedVersion) {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      title = Globals.context.getString(R.string.hasNewVersionHint),
      content = { Text(res.desc) },
      secondaryButton = ButtonConfig.cancelButton(
        onClick = {
          launch {
            SettingsStore.otherSettings.setValue {
              rejectedVersionName = res.version
            }
          }
        }
      ),
      onPrimaryButtonClick = {
        openHttpUrl(Constants.appDownloadUrl)
      }
    ))
  }
}

fun registerTasks()  {
  registerTask(30_1000) {
    coroutineScope.launch {
      val isLoggedIn = AccountStore.isLoggedIn.first()
      if (!isLoggedIn) return@launch
      try {
        AccountStore.checkWaitingNotificationTotal()
      } catch (e: Exception) {
        printRequestErr(e, "检查用户等待通知失败")
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

