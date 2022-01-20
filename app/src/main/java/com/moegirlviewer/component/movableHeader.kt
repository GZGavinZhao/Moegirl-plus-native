package com.moegirlviewer.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import com.google.accompanist.insets.statusBarsHeight

@Composable
fun ListWithMovableHeader(
  maxDistance: Dp,
  statusBarMask: Boolean = true,
  lazyListState: LazyListState,
  header: @Composable () -> Unit,
  content: @Composable ListWithMovableHeaderScope.() -> Unit
) {
  val density = LocalDensity.current
  val themeColors = MaterialTheme.colors
  var placeholderHeight by remember { mutableStateOf(0.dp) }
  var offsetY by remember { mutableStateOf(0f.dp) }
  val lastScrollState = remember {
    object {
      var index = 0
      var offset = 0
      var firstItemHeight = 0
    }
  }
  val isHeaderFloating = !(lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0)
  val listWithMovableHeaderScope = remember(
    placeholderHeight,
    isHeaderFloating
  ) {
    ListWithMovableHeaderScope(
      placeholderHeight = placeholderHeight,
      isHeaderFloating = isHeaderFloating
    )
  }

  LaunchedEffect(lazyListState.firstVisibleItemScrollOffset) {
    fun Dp.ranged() = max(-maxDistance, min(0.dp, this))

    val diffOffset = if (lazyListState.firstVisibleItemIndex == lastScrollState.index) {
      lazyListState.firstVisibleItemScrollOffset - lastScrollState.offset
    } else if(lazyListState.firstVisibleItemIndex > lastScrollState.index) {
      (lastScrollState.firstItemHeight - lastScrollState.offset) + lazyListState.firstVisibleItemScrollOffset
    } else {
      -(
        (lazyListState.layoutInfo.visibleItemsInfo.first().size - lazyListState.firstVisibleItemScrollOffset)
          + lastScrollState.offset
      )
    }

    val diffOffsetDp =  density.run { diffOffset.toDp() }
    offsetY = (offsetY - diffOffsetDp).ranged()

    lastScrollState.index = lazyListState.firstVisibleItemIndex
    lastScrollState.offset = lazyListState.firstVisibleItemScrollOffset
    lastScrollState.firstItemHeight = if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty())
      lazyListState.layoutInfo.visibleItemsInfo.first().size else 0
  }

  Box {
    if (statusBarMask) {
      Spacer(modifier = Modifier
        .zIndex(2f)
        .statusBarsHeight()
        .fillMaxWidth()
        .background(themeColors.primary)
      )
    }

    Box(
      modifier = Modifier
        .zIndex(1f)
        .absoluteOffset(0.dp, offsetY)
        .onGloballyPositioned { placeholderHeight = density.run { it.size.height.toDp() } }
    ) {
      header()
    }

    listWithMovableHeaderScope.run {
      content()
    }
  }
}

class ListWithMovableHeaderScope(
  private val placeholderHeight: Dp,
  private val isHeaderFloating: Boolean
) {
  fun LazyListScope.headerPlaceholder() {
    item {
      Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(placeholderHeight)
      )
    }
  }
}