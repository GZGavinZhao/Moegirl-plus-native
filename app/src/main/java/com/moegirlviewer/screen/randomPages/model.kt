package com.moegirlviewer.screen.randomPages

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.page.bean.GetRandomPageResBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.randomPages.component.RandomPageItemState
import com.moegirlviewer.screen.randomPages.component.RandomPageItemStatus
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.printDebugLog
import com.moegirlviewer.util.printRequestErr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)
class RandomPagesScreenModal @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  var pageList by mutableStateOf(emptyList<GetRandomPageResBean.Query.MapValue>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null
  val randomPageItemStates = listOf(
    RandomPageItemState(),
    RandomPageItemState(),
  ).apply { this[0].connect(this[1]) }

  // 不知道为什么，这里使用挂起函数在LaunchedEffect里调用时，协程被提前终止了
  fun loadPageList() = coroutineScope.launch {
    try {
      status = LoadStatus.LOADING
      val res = PageApi.getRandomPage(20, continueKey)
      pageList = pageList + res.query.pages.values
      continueKey = res.`continue`.grncontinue
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "【随机条目】加载随机条目集失败")
      status = LoadStatus.FAIL
    }
  }

  fun popFirstFromPageList(): GetRandomPageResBean.Query.MapValue? {
    val newPageList = pageList.toMutableList()
    val poppedItem = newPageList.removeFirstOrNull()
    pageList = newPageList
    return poppedItem
  }

  suspend fun nextRandomPage() {
    randomPageItemStates.firstOrNull { it.status == RandomPageItemStatus.RISEN }
      ?.swipeableState
      ?.animateTo(1)
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
  }
}