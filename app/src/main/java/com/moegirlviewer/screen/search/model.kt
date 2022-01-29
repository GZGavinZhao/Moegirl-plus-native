package com.moegirlviewer.screen.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.searchResult.SearchResultRouteArguments
import com.moegirlviewer.store.SearchRecord
import com.moegirlviewer.store.SearchRecordsStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class SearchScreenModel @Inject constructor() : ViewModel()  {
  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  var keywordInputValue by mutableStateOf("")
  val searchRecords = SearchRecordsStore.searchRecords

  fun searchByRecord(record: SearchRecord) {
    coroutineScope.launch {
      delay(500)  // 延迟500毫秒，防止切换页面时搜索历史更换顺序不好看
      SearchRecordsStore.addRecord(record)
    }

    if (record.isPageName) {
      Globals.navController.navigate(ArticleRouteArguments(
        pageName = record.keyword,
      ))
    } else {
      Globals.navController.navigate(SearchResultRouteArguments(
        keyword = record.keyword
      ))
    }
  }

  fun showClearSearchRecordModal() {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        coroutineScope.launch { SearchRecordsStore.clearRecord() }
      }
    ) {
      StyledText(stringResource(id = R.string.delAllSearchRecordHint))
    })
  }

  fun showRemoveSearchRecordModal(keyword: String) {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        coroutineScope.launch { SearchRecordsStore.removeRecord(keyword) }
      }
    ) {
      StyledText(stringResource(id = R.string.delSingleSearchRecordHint))
    })
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
  }
}

