package com.moegirlviewer.compable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.moegirlviewer.util.InitRef

// 在数据来源为flow.collectAsState()时，首次渲染一般是空，这样就导致滚动位置无法正确自动恢复，需要手动恢复。这和方法有时不灵
// 暂时废弃，找到了一种更好的办法，在空数据时用一个临时的lazyListState做代替
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