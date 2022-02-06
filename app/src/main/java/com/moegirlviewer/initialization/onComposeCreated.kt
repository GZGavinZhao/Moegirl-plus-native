package com.moegirlviewer.initialization

import android.util.Log
import androidx.core.net.toUri
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
import java.net.SocketTimeoutException
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
}

private fun checkDeepLink() {
  val deepLink = Globals.activity.intent.dataString ?: return
  val plainRegex = Regex(""".+/(.+)$""")
  val pageIdRegex = Regex("""curid=\d+""")

  when {
    deepLink.contains(pageIdRegex) -> {
      val pageId = deepLink.toUri().getQueryParameter("curid")!!.toInt()
      Globals.navController.navigate(ArticleRouteArguments(
        pageId = pageId
      ))
    }
    deepLink.contains(plainRegex) -> {
      val pageName = plainRegex.find(deepLink)!!.groupValues[1]
      if (pageName.contains(Regex("""^[Mm]ainpage$""")).not()) gotoArticlePage(pageName)
    }
  }
}

private suspend fun checkShortcutIntent() {
  val intentAction = Globals.activity.intent.action ?: ""
  val getShortcutActionRegex = Regex("""^com\.moegirlviewer\.(.+)$""")
  if (intentAction.contains(getShortcutActionRegex)){
    val shortcutAction = getShortcutActionRegex.find(intentAction)!!.groupValues[1]
    when(shortcutAction) {
      "SEARCH" -> Globals.navController.navigate("search")
      "CONTINUE_READ" -> {
        val readingRecord = SettingsStore.otherSettings.getValue { this.readingRecord }.first() ?: return
        Globals.navController.navigate(ArticleRouteArguments(
          pageName = readingRecord.pageName,
          readingRecord = readingRecord
        ))
      }
      "RANDOM" -> {
        val randomPage = PageApi.getRandomPage().query.random.first().title
        gotoArticlePage(randomPage)
      }
    }
  }
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
  try {
    val res = AppApi.getLastVersion()
    val currentVersion = Globals.context.packageManager.getPackageInfo(Globals.context.packageName, 0).versionName
    val rejectedVersion = SettingsStore.otherSettings.getValue { this.rejectedVersionName }.first()

    if (res.version != currentVersion && res.version != rejectedVersion) {
      Globals.commonAlertDialog.show(CommonAlertDialogProps(
        title = Globals.context.getString(R.string.hasNewVersionHint),
        content = { StyledText(res.desc) },
        secondaryButton = ButtonConfig.cancelButton(
          onClick = {
            coroutineScope.launch {
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

