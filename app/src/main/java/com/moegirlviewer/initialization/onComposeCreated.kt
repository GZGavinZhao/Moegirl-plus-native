package com.moegirlviewer.initialization

import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.TargetStore
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.article.ArticleRouteArguments
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
  if (Constants.targetStore != TargetStore.FDROID) {
    coroutineScope.launch { checkNewVersion() }
  }
  coroutineScope.launch { checkShortcutIntent() }
  registerTasks()
  checkDeepLink()

  coroutineScope.launch {
    if (isMoegirl()) {
      MoegirlSplashImageManager.loadConfig()
      MoegirlSplashImageManager.syncImagesByConfig()
    } else {
      HmoeSplashImageManager.loadConfig()
      HmoeSplashImageManager.syncImagesByConfig()
    }
  }
}

private fun checkDeepLink() {
  val deepLink = Globals.activity.intent.deepLink ?: return

  when(deepLink) {
    is PageIdDeepLink -> {
      Globals.navController.navigate(ArticleRouteArguments(
        pageKey = PageIdKey(deepLink.pageId)
      ))
    }
    is PageNameDeepLink -> {
      if (!deepLink.isMainPage) gotoArticlePage(deepLink.pageName)
    }
  }
}

private suspend fun checkShortcutIntent() {
  val shortcutAction = Globals.activity.intent.shortcutAction ?: return
  when(shortcutAction) {
    ShortcutAction.SEARCH -> Globals.navController.navigate("search")
    ShortcutAction.CONTINUE_READ -> {
      val readingRecord = SettingsStore.other.getValue { this.readingRecord }.first() ?: return
      Globals.navController.navigate(ArticleRouteArguments(
        pageKey = PageNameKey(readingRecord.pageName),
        readingRecord = readingRecord
      ))
    }
    ShortcutAction.RANDOM -> {
      Globals.navController.navigate("randomPages")
    }
  }
}

private suspend fun initAccount() {
  if (AccountStore.isLoggedIn.first()) {
    val isValidLogin = AccountStore.checkAccount()
    val isLightRequestMode = SettingsStore.common.getValue { lightRequestMode }.first()
    if (isValidLogin) {
      if (!isLightRequestMode) initUserInfo()
    } else {
      AccountStore.logout()
      toast(Globals.context.getString(R.string.invalidLoginStatusHint))
    }
  }
}

private suspend fun checkNewVersion() = coroutineScope {
  try {
    val res = AppApi.getLastVersion()
    fun String.toVersionNumber() = this.replace(".", "").toInt()
    val currentVersion = Globals.context.packageManager.getPackageInfo(Globals.context.packageName, 0).versionName.toVersionNumber()
    val rejectedVersion = SettingsStore.other.getValue { this.rejectedVersionName }.first()

    if (res.version.toVersionNumber() > currentVersion && res.version != rejectedVersion) {
      Globals.commonAlertDialog.show(CommonAlertDialogProps(
        title = Globals.context.getString(R.string.hasNewVersionHint),
        content = { StyledText(res.desc ?: "") },
        secondaryButton = ButtonConfig.cancelButton(
          onClick = {
            coroutineScope.launch {
              SettingsStore.other.setValue {
                rejectedVersionName = res.version
              }
            }
          }
        ),
        onPrimaryButtonClick = {
          openHttpUrl(res.downloadUrl)
        }
      ))
    }
  } catch (e: MoeRequestException) {
    printRequestErr(e, "初始化检查新版本失败")
  }
}

fun registerTasks()  {
  registerTask(30_1000) {
    coroutineScope.launch {
      val isLoggedIn = AccountStore.isLoggedIn.first()
      if (!isLoggedIn) return@launch
      try {
        AccountStore.checkWaitingNotificationTotal()
      } catch (e: MoeRequestException) {
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

