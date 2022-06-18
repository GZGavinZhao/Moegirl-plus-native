package com.moegirlviewer.screen.newPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.editingRecord.bean.NewPagesBean
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.page.bean.PageProfileBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.PageIdKey
import com.moegirlviewer.util.printRequestErr
import javax.inject.Inject

class NewPagesScreenModel @Inject constructor() : ViewModel() {
  var newPageList by mutableStateOf(emptyList<PageProfileBean.Query.MapValue>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null
  val lazyListState = LazyListState()
  val swipeRefreshState = SwipeRefreshState(true)

  suspend fun loadList(reload: Boolean = false) {
    if (LoadStatus.isCannotLoad(status)) { return }
    status = if (reload) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    if (reload) continueKey = null
    try {
      val newPagesRes = EditingRecordApi.getNewPages(continueKey)
      val newPageIds = newPagesRes.query.recentchanges.map { it.pageid }.toIntArray()
      if (newPageIds.isEmpty()) {
        status = LoadStatus.ALL_LOADED
        return
      }

      val pagesProfileRes = PageApi.getPageProfile(PageIdKey(*newPageIds))
      val resultList = pagesProfileRes.query.pages.values.toList()
        .filter { it.ns == 0 }   // 如果其中有条目被打回用户页，会出现newPages接口返回页面为条目，pagesProfile返回页面为用户页的情况，这里需要额外过滤
        .sortedBy { it.pageid }
        .reversed()
      newPageList = if (reload) resultList else newPageList + resultList
      continueKey = newPagesRes.`continue`?.rccontinue
      status = if (continueKey != null) LoadStatus.SUCCESS else LoadStatus.ALL_LOADED
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载最新条目列表失败")
      status = LoadStatus.FAIL
    }
  }

  override fun onCleared() {
    super.onCleared()
  }
}