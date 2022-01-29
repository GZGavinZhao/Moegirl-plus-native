package com.moegirlviewer.util

import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.store.AccountStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first

/**
 * @throws [NotLoggedInException] 未登录
 */
suspend fun checkIsLoggedIn(message: String) {
  val isLoggedIn = AccountStore.isLoggedIn.first()
  if (!isLoggedIn) {
    val deferred = CompletableDeferred<Boolean>()
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(message)
      },
      secondaryButton = ButtonConfig.cancelButton {
        deferred.complete(false)
      },
      onPrimaryButtonClick = {
        Globals.navController.navigate("login")
        deferred.complete(true)
      }
    ))

    throw NotLoggedInException(deferred.await())
  }
}

class NotLoggedInException(
  val gotoLoginClicked: Boolean
) : Exception() {
  override fun toString(): String {
    return "未登录拦截：${if (gotoLoginClicked) "点击前往登录" else "取消前往登录"}"
  }
}