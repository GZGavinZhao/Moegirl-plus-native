package com.moegirlviewer.component.customDrawer

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.toDp
import com.moegirlviewer.util.visibility
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomDrawerRef(
  val open: () -> Unit,
  val close: () -> Unit,
)

// 因官方的ModalDrawer组件开启手势全屏有效，且无法配置，这里只好自己实现一个
@ExperimentalMaterialApi
@Composable
fun CustomDrawer(
  width: Dp = (LocalConfiguration.current.screenWidthDp * 0.6).dp,
  gestureWidth: Dp = 20.dp,
  alwaysDelayInitialize: Boolean = false,   // 因为每次初始化渲染时立刻渲染总会闪现一下，有时需要延迟渲染避免这个问题，酌情使用这个选项
  side: CustomDrawerSide = CustomDrawerSide.LEFT,
  drawerContent: @Composable (ref: CustomDrawerRef) -> Unit,
  ref: Ref<CustomDrawerRef>? = null,
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val swipeableState = rememberSwipeableState(
    initialValue = 0,
    animationSpec = tween(
      durationMillis = 150,
      delayMillis = 0,
      easing = LinearEasing,
    ),
  )
  var delayDisplayFlag by rememberSaveable { mutableStateOf(false) }
  var alwaysDelayDisplayFlag by remember { mutableStateOf(false) }
  // 不知道是不是bug，swipeableState恢复时，swipeableState.offset没有根据anchors的值恢复，而是0，这里只好加个状态用于动画值的判断
  var swipableStateRestored by remember { mutableStateOf(!delayDisplayFlag) }

  val isLeftSide = side == CustomDrawerSide.LEFT
  val widthPx = density.run { width.toPx() }
  val anchors = remember { mapOf(
      (if (isLeftSide) -widthPx else widthPx) to 0,
      0f to 1
  ) }
  val swipeOffset = if (swipableStateRestored)
    swipeableState.offset.value.toDp() else
    anchors.keys.first().dp
  val animationProgress = if (swipableStateRestored)
    1 - swipeableState.offset.value / anchors.keys.first() else
    0f

  fun open() {
    scope.launch { swipeableState.animateTo(1) }
  }

  fun close() {
    scope.launch { swipeableState.animateTo(0) }
  }

  SideEffect {
    ref?.value = CustomDrawerRef(
      open = { open() },
      close = { close() }
    )
  }

  // 初次渲染时会出现闪现的情况，这里延迟1秒显示
  LaunchedEffect(true) {
    delay(500)
    delayDisplayFlag = true
    alwaysDelayDisplayFlag = true
  }

  LaunchedEffect(swipeableState.offset.value) {
    swipableStateRestored = true
  }

  val displayFlag = if (alwaysDelayInitialize) alwaysDelayDisplayFlag else delayDisplayFlag

  BackHandler(swipeableState.currentValue == 1) { close() }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = if (isLeftSide) Alignment.TopStart else Alignment.TopEnd
  ) {
    content()

    if (animationProgress != 0f && displayFlag) {
      Spacer(
        modifier = Modifier
          .fillMaxSize()
          .background(
            color = Color(0f, 0f, 0f, 0.5f * animationProgress),
          )
          .noRippleClickable { close() }
      )
    }

    Box(
      modifier = Modifier
        .width(width + gestureWidth)
        .fillMaxHeight()
        .offset(x = swipeOffset)
        .visibility(animationProgress != 0f && displayFlag)
    ) {
      Box(
        modifier = Modifier
          .matchParentSize()
          .swipeable(
            state = swipeableState,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.2f) },
            orientation = Orientation.Horizontal,
            resistance = null
          ),
        contentAlignment = if (isLeftSide) Alignment.TopStart else Alignment.TopEnd
      ) {
        Box(
          modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(themeColors.background)
        ) {
          Surface(
            modifier = Modifier
              .matchParentSize(),
            elevation = (20 * animationProgress).dp
          ) {
            drawerContent(
              CustomDrawerRef(
                open = { open() },
                close = { close() }
              )
            )
          }
        }
      }
    }
  }
}

enum class CustomDrawerSide {
  LEFT,
  RIGHT
}