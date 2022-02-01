package com.moegirlviewer.screen.splashPreview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashPreviewScreenModel @Inject constructor() : ViewModel() {
  val contentAlpha = Animatable(0f)
  val imageScale = Animatable(1.2f)

  suspend fun showAppearAnimation() {
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

  suspend fun showHideAnimation() {
    contentAlpha.animateTo(
      targetValue = 0f,
      animationSpec = tween(
        durationMillis = 350
      )
    )

    imageScale.snapTo(1.2f)
  }

  override fun onCleared() {
    super.onCleared()
  }
}