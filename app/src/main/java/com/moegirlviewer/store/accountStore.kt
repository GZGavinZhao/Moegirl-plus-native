package com.moegirlviewer.store

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.moegirlviewer.DataStoreName
import com.moegirlviewer.api.account.AccountApi
import com.moegirlviewer.api.account.bean.UserInfoBean
import com.moegirlviewer.api.edit.EditApi
import com.moegirlviewer.api.notification.NotificationApi
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.request.MoeRequestTimeoutException
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.initUserInfo
import com.moegirlviewer.util.printRequestErr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

typealias UserInfo = UserInfoBean.Query.Userinfo

object AccountStore {
  private val Context.dataStore by preferencesDataStore(DataStoreName.ACCOUNT.name)
  private val dataStore get() = Globals.context.dataStore
  private val dataStoreKeys = object {
    val userName = stringPreferencesKey("userName")
  }

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  val userName get() = dataStore.data.map { it[dataStoreKeys.userName] }
  val waitingNotificationTotal = MutableStateFlow(0)
  val userInfo = MutableStateFlow<UserInfo?>(null)

  val isLoggedIn get() = userName.map { it != null }

  suspend fun login(userName: String, password: String): LoginResult {
    val res = AccountApi.login(userName, password)
    return if (res.clientlogin.status == "PASS") {
      dataStore.updateData {
        it.toMutablePreferences().apply {
          this[dataStoreKeys.userName] = userName
        }
      }

      initUserInfo()

      LoginResult(true)
    } else {
      LoginResult(
        success = false,
        message = res.clientlogin.message
      )
    }
  }

  fun logout() {
    userInfo.value = null
    try {
      coroutineScope.launch { AccountApi.logout() }
    } catch (e: MoeRequestException) {}
    coroutineScope.launch {
      dataStore.updateData {
        it.toMutablePreferences().apply { this.remove(dataStoreKeys.userName) }
      }
    }
  }

  suspend fun checkAccount(): Boolean {
    return try {
      val csrfToken = EditApi.getCsrfToken()
      csrfToken != "+\\"
    } catch (e: MoeRequestException) {
      printRequestErr(e, "检查账户有效性失败")
      true // 因为萌百服务器不稳定，所以将网络超时认定为有效
    }
  }

  suspend fun checkWaitingNotificationTotal() {
    val res = NotificationApi.getList(limit = 1)
    waitingNotificationTotal.value = res.query.notifications.rawcount
  }

  suspend fun markAllNotificationAsRead() {
    NotificationApi.markAllAsRead()
    waitingNotificationTotal.value = 0
  }

  /**
   * @exception LoadUserInfoException
   */
  suspend fun loadUserInfo(): UserInfo {
    if (userInfo.value != null) return userInfo.value!!
    val userInfoRes = AccountApi.getInfo()
    userInfo.value = userInfoRes.query.userinfo
    return userInfo.value!!
  }

  suspend fun isInUserGroup(userGroup: UserGroup): Boolean {
    val userInfo = loadUserInfo()
    return userInfo.groups.contains(userGroup.code)
  }
}

class LoginResult(
  val success: Boolean,
  val message: String? = null
)

enum class UserGroup(
  val code: String
) {
  AUTO_CONFIRMED("autoconfirmed"),
  GOOD_EDITOR("goodeditor"),
  PATROLLER("patroller")
}