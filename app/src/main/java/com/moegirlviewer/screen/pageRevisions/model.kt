package com.moegirlviewer.screen.pageRevisions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.editingRecord.bean.PageRevisionsBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.PageNameKey
import com.moegirlviewer.util.printRequestErr
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PageRevisionsScreenModel @Inject constructor() : ViewModel() {
  lateinit var routeArguments: PageRevisionsRouteArguments
  val swipeRefreshState = SwipeRefreshState(true)
  val lazyListState = LazyListState()
  var revisionList by mutableStateOf(emptyList<PageRevisionsBean.Query.MapValue.Revision>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null

  suspend fun loadRevisionList(
    refresh: Boolean = false
  ) {
    if (LoadStatus.isCannotLoad(status) && !refresh) return

    status = if (refresh) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    if (refresh) continueKey = null

    try {
      val res = EditingRecordApi.getPageRevisions(
        pageKey = PageNameKey(routeArguments.pageName),
        continueKey = continueKey
      )
      if (res.query.pages.values.first().missing != null) {
        status = LoadStatus.EMPTY
        return
      }

      val list = res.query.pages.values.first().revisions!!
      val nextContinueKey = res.`continue`?.rvcontinue
      val nexStatus = when {
        nextContinueKey == null && list.isNotEmpty() -> LoadStatus.ALL_LOADED
        list.isEmpty() -> LoadStatus.EMPTY
        else -> LoadStatus.SUCCESS
      }

      revisionList = if (refresh) list else revisionList + list
      status = nexStatus
      continueKey = nextContinueKey
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载页面修订列表失败")
      status = LoadStatus.FAIL
    }
  }

  override fun onCleared() {
    super.onCleared()
    routeArguments.removeReferencesFromArgumentPool()
  }
}