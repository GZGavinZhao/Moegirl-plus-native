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
import com.moegirlviewer.screen.home.component.RandomPageCardState
import com.moegirlviewer.screen.home.component.RecommendationCardState
import com.moegirlviewer.screen.home.component.newPagesCard.NewPagesCardState
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

  val randomPageCardState = RandomPageCardState()
  val newPagesCardState = NewPagesCardState()
  val recommendationCardState = RecommendationCardState()

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

  suspend fun loadCardsData() = coroutineScope {
    cardsDataStatus = LoadStatus.LOADING
    listOf(
      launch { randomPageCardState.reload() },
      launch { newPagesCardState.reload() },
      launch { recommendationCardState.reload() }
    ).forEach { it.join() }
    cardsDataStatus = LoadStatus.SUCCESS
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

abstract class HomeScreenCardState {
  abstract suspend fun reload()
}