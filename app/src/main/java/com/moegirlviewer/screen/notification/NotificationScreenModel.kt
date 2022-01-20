package com.moegirlviewer.screen.notification

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.moegirlviewer.R
import com.moegirlviewer.api.notification.NotificationApi
import com.moegirlviewer.api.notification.bean.NotificationListBean
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

typealias Notification = NotificationListBean.Query.Notifications.Notification

@HiltViewModel
class NotificationScreenModel @Inject constructor() : ViewModel() {
  var notificationList by mutableStateOf(emptyList<Notification>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null
  val swipeRefreshState = SwipeRefreshState(true)
  val lazyListState = LazyListState()

  suspend fun loadList(refresh: Boolean = false) {
    if (LoadStatus.isCannotLoad(status)) return
    status = if (refresh) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    if (refresh) notificationList = emptyList()

    try {
      val res = NotificationApi.getList(if (refresh) null else continueKey)
      val notificationData = res.query.notifications
      val nextStatus = when {
        notificationData.`continue` == null && notificationData.list.isNotEmpty() -> LoadStatus.ALL_LOADED
        notificationData.list.isEmpty() -> LoadStatus.EMPTY
        else -> LoadStatus.SUCCESS
      }

      notificationList = notificationList + notificationData.list.reversed()
      status = nextStatus
      continueKey = notificationData.`continue`
    } catch (e: Exception) {
      printRequestErr(e, "加载通知列表失败")
      status = LoadStatus.FAIL
    }
  }

  suspend fun markAllAsRead() {
    Globals.commonLoadingDialog.showText(Globals.context.getString(R.string.submitting))
    try {
      AccountStore.markAllNotificationAsRead()
      notificationList = notificationList.map {
        it.read = ""
        it
      }
      toast(Globals.context.getString(R.string.markAllAsReaded))
    } catch (e: Exception) {
      printRequestErr(e, "标记全部通知为已读失败")
      toast(e.toString())
    } finally {
      Globals.commonLoadingDialog.hide()
    }
  }

  override fun onCleared() {
    super.onCleared()
  }
}