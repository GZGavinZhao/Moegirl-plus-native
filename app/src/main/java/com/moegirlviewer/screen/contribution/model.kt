package com.moegirlviewer.screen.contribution

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.editingRecord.bean.UserContributionBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ContributionScreenModel @Inject constructor() : ViewModel() {
  lateinit var routeArguments: ContributionRouteArguments
  val swipeRefreshState = SwipeRefreshState(true)
  val lazyListState = LazyListState()
  var contributionList by mutableStateOf(emptyList<UserContributionBean.Query.Usercontrib>())
  var startDate by mutableStateOf(LocalDate.now().minusDays(6))
  var endDate by mutableStateOf(LocalDate.now())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null

  suspend fun loadContributionList(refresh: Boolean = false) {
    if (LoadStatus.isCannotLoad(status) && !refresh) return
    if (refresh) continueKey = null

    status = if (refresh) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    try {
      val res = EditingRecordApi.getUserContribution(
        userName = routeArguments.userName,
        startISO = endDate.toEpochMilli().toLocalDateTime().format(moegirlNormalTimestampDateFormatter),
        endISO = startDate.toEpochMilli().toLocalDateTime().format(moegirlNormalTimestampDateFormatter),
        continueKey = continueKey
      )

      val list = res.query.usercontribs
      val nextContinueKey = res.`continue`?.uccontinue
      val nexStatus = when {
        nextContinueKey == null && list.isNotEmpty() -> LoadStatus.ALL_LOADED
        list.isEmpty() -> LoadStatus.ALL_LOADED
        else -> LoadStatus.SUCCESS
      }

      contributionList = if (refresh) list else contributionList + list
      status = nexStatus
      continueKey = nextContinueKey
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载用户贡献列表失败")
      status = LoadStatus.FAIL
    }
  }

  suspend fun showDatePickerDialog(isStartDate: Boolean = true) {
    val result = Globals.commonDatePickerDialog.show(
      initialValue = if (isStartDate) startDate else endDate,
      minDate = LocalDate.of(2010, 1, 1),
      maxDate = LocalDate.now()
    ) ?: return

    if (isStartDate) {
      startDate = result

    } else {
      endDate = result
    }

    if (startDate.isAfter(endDate)) {
      val endDateBackup = endDate
      endDate = startDate
      startDate = endDateBackup
    }

    lazyListState.scrollToItem(0)
    loadContributionList(true)
  }

  override fun onCleared() {
    super.onCleared()
    routeArguments.removeReferencesFromArgumentPool()
  }
}