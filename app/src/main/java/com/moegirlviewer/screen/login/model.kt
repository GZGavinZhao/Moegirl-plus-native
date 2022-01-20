package com.moegirlviewer.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.Globals.navController
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenModel @Inject constructor() : ViewModel() {
  var userName by mutableStateOf("")
  var password by mutableStateOf("")

  suspend fun submit() {
    Globals.context.run {
      if (userName.isEmpty()) return toast(getString(R.string.userNameEmptyHint))
      if (password.isEmpty()) return toast(getString(R.string.passwordEmptyHint))

      Globals.commonLoadingDialog.show()
      try {
        val result = AccountStore.login(userName, password)
        if (result.success) {
          toast(getString(R.string.loggedIn))
          navController.popBackStack()
        } else {
          toast(result.message!!)
        }
      } catch(e: Exception) {
        printRequestErr(e, "登录错误")
      } finally {
        Globals.commonLoadingDialog.hide()
      }
    }
  }
}