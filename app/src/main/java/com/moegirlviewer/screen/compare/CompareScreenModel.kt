package com.moegirlviewer.screen.compare

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.moegirlviewer.R
import com.moegirlviewer.api.edit.EditApi
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.editingRecord.bean.ComparePageResult
import com.moegirlviewer.screen.compare.util.DiffLine
import com.moegirlviewer.screen.compare.util.collectDiffBlocksFormHtml
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagerApi
class CompareScreenModel @Inject constructor() : ViewModel() {
  lateinit var routeArguments: CompareRouteArguments
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val pagerState = PagerState(2)
  var compareData by mutableStateOf<ComparePageResult.Compare?>(null)
  var leftLines by mutableStateOf(emptyList<DiffLine>())
  var rightLines by mutableStateOf(emptyList<DiffLine>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var selectedTabIndex by mutableStateOf(0)

  val isCompareTextMode get() = routeArguments is CompareTextRouteArguments
  val routeArgumentsOfPageCompare get() = routeArguments as ComparePageRouteArguments
  val routeArgumentsOfTextCompare get() = routeArguments as CompareTextRouteArguments

  suspend fun loadCompareData() {
    status = LoadStatus.LOADING
    try {
      val res = if (isCompareTextMode) {
        EditingRecordApi.comparePage(
          fromText = routeArgumentsOfTextCompare.formText,
          toText = routeArgumentsOfTextCompare.toText
        )
      } else {
        EditingRecordApi.comparePage(
          fromTitle = routeArgumentsOfPageCompare.pageName,
          fromRev = routeArgumentsOfPageCompare.fromRevId,
          toTitle = routeArgumentsOfPageCompare.pageName,
          toRev = routeArgumentsOfPageCompare.toRevId
        )
      }

      val diffBlocks = withContext(Dispatchers.Default) {
        collectDiffBlocksFormHtml("<table>${res.compare._asterisk}</table>")
      }

      compareData = res.compare
      leftLines = diffBlocks.map { it.left }
      rightLines = diffBlocks.map { it.right }
      status = LoadStatus.SUCCESS
    } catch (e: Exception) {
      printRequestErr(e, "加载页面差异数据失败")
      status = LoadStatus.FAIL
    }
  }

  suspend fun submitUndo(summary: String): Boolean {
    val userName = compareData!!.touser
    val toRevId = routeArgumentsOfPageCompare.toRevId
    val summaryPrefix = Globals.context.getString(R.string.comparesummaryPrefix, userName, toRevId.toString())
    val fullSummary = summaryPrefix + " " +
      Globals.context.getString(R.string.undoReason) +
      "：${if (summary != "") summary else Globals.context.getString(R.string.reasonNoFilled)}"

    Globals.commonLoadingDialog.showText(Globals.context.getString(R.string.submitting))
    return try {
      EditApi.edit(
        pageName = routeArgumentsOfPageCompare.pageName,
        summary = fullSummary,
        undoRevId = toRevId,
        baseDateISO = EditApi.getTimestampOfLastEdit(routeArgumentsOfPageCompare.pageName)
      )
      toast(Globals.context.getString(R.string.undid))
      true
    } catch (e: Exception) {
      printRequestErr(e, "执行撤销失败")
      toast(e.toString())
      false
    } finally {
      Globals.commonLoadingDialog.hide()
    }
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    routeArguments.removeReferencesFromArgumentPool()
  }
}