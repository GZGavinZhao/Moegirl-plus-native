package com.moegirlviewer.screen.searchResult

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moegirlviewer.api.search.SearchApi
import com.moegirlviewer.screen.searchResult.component.SearchResultItem
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.printRequestErr
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultScreenModel @Inject constructor() : ViewModel()  {
  var resultList by mutableStateOf<List<SearchResultItem>>(emptyList())
  lateinit var routeArguments: SearchResultRouteArguments
  var resultTotal by mutableStateOf(-1)
  var status by mutableStateOf(LoadStatus.INITIAL)
  val listState = LazyListState()

  suspend fun loadList() {
    if (LoadStatus.isCannotLoad(status)) { return }
    status = LoadStatus.LOADING

    try {
      val res = SearchApi.search(routeArguments.keyword, resultList.size)
      if (res.query.searchinfo.totalhits == 0) {
        status = LoadStatus.EMPTY
        return
      }

      val nextStatus = if (res.query.searchinfo.totalhits == resultList.size + res.query.search.size) {
        LoadStatus.ALL_LOADED
      } else {
        LoadStatus.SUCCESS
      }

      resultTotal = res.query.searchinfo.totalhits
      resultList = resultList + res.query.search
      status = nextStatus
    } catch(e: Exception) {
      printRequestErr(e, "加载搜索结果失败")
      status = LoadStatus.FAIL
    }
  }

  override fun onCleared() {
    super.onCleared()
    routeArguments.removeReferencesFromArgumentPool()
  }
}
