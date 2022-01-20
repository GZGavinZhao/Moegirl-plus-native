package com.moegirlviewer.screen.home

import androidx.compose.ui.node.Ref
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.articleView.ArticleViewRef
import com.moegirlviewer.util.CachedWebViews
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val cachedWebViews = CachedWebViews()
  val memoryStore = MemoryStore()
  val articleViewRef = Ref<ArticleViewRef>()

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

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    cachedWebViews.destroyAllInstance()
  }

  companion object {
    var needReload = false
  }
}