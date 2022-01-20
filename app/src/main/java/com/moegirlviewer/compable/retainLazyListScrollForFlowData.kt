package com.moegirlviewer.compable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.moegirlviewer.util.InitRef

// 在数据来源为flow.collectAsState()时，首次渲染一般是空，这样就导致滚动位置无法正确自动恢复，需要手动恢复
@Composable
fun LazyListState.RetainScroll(flowReady: Boolean) {
  val savedPosition = rememberSaveable { mutableMapOf(
    "index" to 0,
    "offset" to 0
  ) }
  val isRetained = remember { InitRef(false) }

  DisposableEffect(true) {
    onDispose {
      savedPosition["index"] = this@RetainScroll.firstVisibleItemIndex
      savedPosition["offset"] = this@RetainScroll.firstVisibleItemScrollOffset
    }
  }

  LaunchedEffect(flowReady) {
    if(flowReady && !isRetained.value) {
      this@RetainScroll.scrollToItem(savedPosition["index"]!!, savedPosition["offset"]!!)
      isRetained.value = true
    }
  }
}