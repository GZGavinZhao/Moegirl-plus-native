package com.moegirlviewer.screen.commentReply

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.node.Ref
import androidx.lifecycle.ViewModel
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.screen.comment.component.commentItem.CommentScreenCommentItemRef
import com.moegirlviewer.util.CachedWebViews
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import javax.inject.Inject

@HiltViewModel
class CommentReplyScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val memoryStore = MemoryStore()
  lateinit var routeArguments: CommentReplyRouteArguments
  val lazyListState = LazyListState()
  val itemRefs = mutableMapOf<String, Ref<CommentScreenCommentItemRef>>()

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    routeArguments.removeReferencesFromArgumentPool()
  }
}