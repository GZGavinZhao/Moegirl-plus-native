package com.moegirlviewer.component.customDrawer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moegirlviewer.compable.DoSideEffect
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.toDp
import com.moegirlviewer.util.visibility
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// 因官方的ModalDrawer组件开启手势全屏有效，且无法配置，这里只好自己实现一个
@ExperimentalMaterialApi
@Composable
fun CustomDrawer(
  state: CustomDrawerState = remember { CustomDrawerState() },
  width: Dp = (LocalConfiguration.current.screenWidthDp * 0.6).dp,
  gestureWidth: Dp = 20.dp,
  alwaysDelayInitialize: Boolean = false,   // 因为每次初始化渲染时立刻渲染总会闪现一下，有时需要延迟渲染避免这个问题，酌情使用这个选项
  side: CustomDrawerSide = CustomDrawerSide.LEFT,
  drawerContent: @Composable () -> Unit,
  onAnimationProgressChanged: ((Float) -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  val isLeftSide = side == CustomDrawerSide.LEFT
  val widthPx = remember(width) { density.run { width.toPx() } }
  val anchors = remember { mapOf(
      (if (isLeftSide) -widthPx else widthPx) to 0,
      0f to 1
  ) }

  DoSideEffect {
    state.anchors = anchors
  }

  // 初次渲染时会出现闪现的情况，这里延迟显示
  LaunchedEffect(true) {
    delay(500)
    state.delayDisplayFlag = true
    state.alwaysDelayDisplayFlag = true
  }

  LaunchedEffect(state.swipeableState.offset.value) {
    state.swipableStateRestored = true
  }

  val displayFlag = if (alwaysDelayInitialize) state.alwaysDelayDisplayFlag else state.delayDisplayFlag

  BackHandler(state.swipeableState.currentValue == 1) {
    scope.launch { state.close() }
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = if (isLeftSide) Alignment.TopStart else Alignment.TopEnd
  ) {
    content()

    if (state.animationProgress != 0f && displayFlag) {
      Spacer(
        modifier = Modifier
          .fillMaxSize()
          .background(
            color = Color(0f, 0f, 0f, 0.5f * state.animationProgress),
          )
          .noRippleClickable {
            scope.launch { state.close() }
          }
      )
    }

    Box(
      modifier = Modifier
        .width(width + gestureWidth)
        .fillMaxHeight()
        .visibility(state.animationProgress != 0f && displayFlag)
        .graphicsLayer(
          translationX = state.swipeOffset
        )
    ) {
      Box(
        modifier = Modifier
          .matchParentSize()
          .swipeable(
            state = state.swipeableState,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.4f) },
            orientation = Orientation.Horizontal,
            resistance = null,
            velocityThreshold = SwipeableDefaults.VelocityThreshold / 2,
          ),
        contentAlignment = if (isLeftSide) Alignment.TopStart else Alignment.TopEnd
      ) {
        Box(
          modifier = Modifier
            .width(width)
            .fillMaxHeight()
        ) {
          Surface(
            modifier = Modifier
              .matchParentSize(),
            elevation = (20 * state.animationProgress).dp
          ) {
            drawerContent()
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
class CustomDrawerState {
  lateinit var anchors: Map<Float, Int>

  val swipeableState = SwipeableState(
    initialValue = 0,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMediumLow
    )
  )
  var delayDisplayFlag by mutableStateOf(false)
  var alwaysDelayDisplayFlag by mutableStateOf(false)
  // 不知道是不是bug，swipeableState恢复时，swipeableState.offset没有根据anchors的值恢复，而是0，这里只好加个状态用于动画值的判断
  var swipableStateRestored by mutableStateOf(!delayDisplayFlag)

  val swipeOffset get(): Float {
    return if (swipableStateRestored)
      swipeableState.offset.value else
      anchors.keys.first()
  }

  val animationProgress get(): Float {
    if (!this::anchors.isInitialized) return 0f
    return if (swipableStateRestored)
      1 - swipeableState.offset.value / anchors.keys.first() else
      0f
  }

  suspend fun open() {
    swipeableState.animateTo(1)
  }

  suspend fun close() {
    swipeableState.animateTo(0)
  }
}

enum class CustomDrawerSide {
  LEFT,
  RIGHT
}