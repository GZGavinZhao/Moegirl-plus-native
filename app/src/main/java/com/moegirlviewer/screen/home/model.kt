package com.moegirlviewer.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.node.Ref
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.moegirlviewer.R
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.page.bean.GetRandomPageResBean
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.articleView.ArticleViewRef
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class HomeScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val cachedWebViews = CachedWebViews()
  val memoryStore = MemoryStore()
  val articleViewRef = Ref<ArticleViewRef>()
  val swipeRefreshState = SwipeRefreshState(true)
  var cardsDataStatus by mutableStateOf(LoadStatus.INITIAL)

  var randomPageData by mutableStateOf(CardData<GetRandomPageResBean.Query.MapValue>())

  private var twoPressBackFlag = false
  fun triggerForTwoPressToExit() {
    if (!twoPressBackFlag) {
      twoPressBackFlag = true
      toast(Globals.context.getString(R.string.doubleBackToExit))

      coroutineScope.launch {
        delay(3000)
        twoPressBackFlag = false
      }
    } else {
      Globals.activity.finishAndRemoveTask()
    }
  }

  suspend fun loadCardsData() {
    coroutineScope {
      cardsDataStatus = LoadStatus.LOADING
      val jobs = listOf(
        async { loadRandomPageData() }
      )

      jobs.forEach { it.await() }
      cardsDataStatus = LoadStatus.SUCCESS
    }
  }

  suspend fun loadRandomPageData() {
    randomPageData = randomPageData.copy(status = LoadStatus.LOADING)
    try {
      val res = PageApi.getRandomPage()
      randomPageData = randomPageData.copy(
        body = res.query.pages.values.first(),
        status = LoadStatus.SUCCESS
      )
    } catch (e: MoeRequestException) {
      randomPageData = randomPageData.copy(status = LoadStatus.FAIL)
      printRequestErr(e, "加载随机页面卡片数据失败")
    }
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    cachedWebViews.destroyAllInstance()
  }

  companion object {
    var needReload = false
  }
}

data class CardData<T>(
  val body: T? = null,
  val status: LoadStatus = LoadStatus.INITIAL
)