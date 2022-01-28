package com.moegirlviewer.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

class SplashScreenState(
  // compose的动画需要compose的协程上下文
  val composeCoroutineScope: CoroutineScope
) {
  internal val contentAlpha = Animatable(1f)
  internal val imageScale = Animatable(1.2f)

  suspend fun showAppearAnimation() = withContext(composeCoroutineScope.coroutineContext) {
    imageScale.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = 3000,
      )
    )
  }

  suspend fun showTransparentAnimation() = withContext(composeCoroutineScope.coroutineContext) {
    contentAlpha.animateTo(
      targetValue = 0f,
      animationSpec = tween(
        durationMillis = 350
      )
    )
  }
}