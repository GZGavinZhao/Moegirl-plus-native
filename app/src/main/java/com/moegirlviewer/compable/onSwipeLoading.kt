package com.moegirlviewer.compable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope

@Composable
fun ScrollState.OnSwipeLoading(
  bottomDistance: Int = 200,
  handler: () -> Unit
) {
  LaunchedEffect(this.value) {
    val state = this@OnSwipeLoading
    if (state.maxValue - state.value <= bottomDistance) handler()
  }
}

@Composable
fun LazyListState.OnSwipeLoading(
  stock: Int = 2,
  handler: () -> Unit,
) {
  LaunchedEffect(this.firstVisibleItemScrollOffset) {
    if (
      this@OnSwipeLoading.firstVisibleItemIndex != 0 &&
      this@OnSwipeLoading.layoutInfo.totalItemsCount - stock <= this@OnSwipeLoading.layoutInfo.visibleItemsInfo.last().index
    ) {
      handler()
    }
  }
}