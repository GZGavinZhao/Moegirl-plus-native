package com.moegirlviewer.screen.settings

import androidx.compose.material.Text
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.request.MoeTimeoutException
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.toast
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
          Text(Globals.context.getString(R.string.logoutHint))
        },
        secondaryButton = ButtonConfig.cancelButton(),
        onPrimaryButtonClick = {
          try {
            AccountStore.logout()
          } catch (e: MoeTimeoutException) {
            printRequestErr(e, "调用登出接口超时")
          }

          toast(Globals.context.getString(R.string.logouted))
        }
      ))
    } else {
      Globals.navController.navigate("login")
    }
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
  }
}