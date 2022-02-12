package com.moegirlviewer.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.moegirlviewer.util.SplashImage
import com.moegirlviewer.util.isMoegirl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

class SplashScreenState(
  // compose的动画需要compose的协程上下文
  private val composeCoroutineScope: CoroutineScope,
  internal val splashImage: SplashImage
) {
  internal val contentAlpha = Animatable(0f)
  internal val imageScale = Animatable(1.2f)

  suspend fun showAppearAnimation() = withContext(composeCoroutineScope.coroutineContext) {
    if (isMoegirl()) {
      contentAlpha.animateTo(
        targetValue = 1f,
        animationSpec = tween(
          delayMillis = 1000,
          durationMillis = 300
        )
      )

      imageScale.animateTo(
        targetValue = 1f,
        animationSpec = tween(
          durationMillis = 2800,
        )
      )
    } else {
      contentAlpha.animateTo(
        targetValue = 1f,
        animationSpec = tween(
          durationMillis = 350
        )
      )

      imageScale.animateTo(
        targetValue = 1f,
        animationSpec = tween(
          durationMillis = 2650,
        )
      )
    }
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