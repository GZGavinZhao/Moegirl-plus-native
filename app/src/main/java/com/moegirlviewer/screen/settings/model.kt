package com.moegirlviewer.screen.settings

import androidx.lifecycle.ViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SettingsScreenModel @Inject constructor(): ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)

  suspend fun toggleLoginStatus() {
    if (AccountStore.isLoggedIn.first()) {
      Globals.commonAlertDialog.show(CommonAlertDialogProps(
        content = {
         StyledText(Globals.context.getString(R.string.logoutHint))
        },
        secondaryButton = ButtonConfig.cancelButton(),
        onPrimaryButtonClick = {
          try {
            AccountStore.logout()
          } catch (e: MoeRequestException) {
            printRequestErr(e, "调用登出接口失败")
          }

          toast(Globals.context.getString(R.string.logouted))
        }
      ))
    } else {
      Globals.navController.navigate("login")
    }
  }

  suspend fun checkNewVersion() {
    Globals.commonLoadingDialog.show()
    try {
      val res = AppApi.getLastVersion()
      val currentVersion = Globals.context.packageManager.getPackageInfo(Globals.context.packageName, 0).versionName
      if (res.version != currentVersion) {
        Globals.commonAlertDialog.show(CommonAlertDialogProps(
          title = Globals.context.getString(R.string.hasNewVersionHint),
          content = { StyledText(res.desc ?: "") },
          secondaryButton = ButtonConfig.cancelButton(),
          onPrimaryButtonClick = {
            openHttpUrl(res.downloadUrl)
          }
        ))
      } else {
        toast(Globals.context.getString(R.string.currentIsVersion))
      }
    } catch (e: MoeRequestException) {
      toast(Globals.context.getString(R.string.netErr))
    } finally {
      Globals.commonLoadingDialog.hide()
    }
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
  }
}