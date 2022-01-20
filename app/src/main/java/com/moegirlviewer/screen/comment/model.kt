package com.moegirlviewer.screen.comment

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.screen.comment.component.commentEditor.CommentEditorController
import com.moegirlviewer.screen.comment.component.commentEditor.CommentEditorExceptionByUserClosed
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.util.CachedWebViews
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class CommentScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val memoryStore = MemoryStore()
  lateinit var routeArguments: CommentRouteArguments
  val lazyListState = LazyListState()
  val swipeRefreshState = SwipeRefreshState(true)

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    routeArguments.removeReferencesFromArgumentPool()
  }
}