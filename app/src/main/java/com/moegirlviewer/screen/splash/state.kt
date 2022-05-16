package com.moegirlviewer.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.accompanist.systemuicontroller.SystemUiController
import com.moegirlviewer.screen.splashSetting.SplashImageMode
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SplashScreenState {
  var splashImage by mutableStateOf<SplashImage?>(null)
  var visible by mutableStateOf(true)
  val imageScale = Animatable(1.2f)

  suspend fun isShowSplashScreen(): Boolean {
    val intent = Globals.activity.intent
    val hasDeepLink = intent.dataString != null
    val hasShortcutAction = intent.shortcutAction != null
    val splashImageMode = SettingsStore.common.getValue { this.splashImageMode }.first()
    return !hasDeepLink &&
      !hasShortcutAction &&
      splashImageMode != SplashImageMode.OFF
  }

  suspend fun getUsingSplashImage(): SplashImage {
    return if (isMoegirl()) {
      val splashImageMode = SettingsStore.common.getValue { this.splashImageMode }.first()
      when(splashImageMode) {
        SplashImageMode.NEW -> MoegirlSplashImageManager.getLatestImage()
        SplashImageMode.RANDOM -> MoegirlSplashImageManager.getRandomImage()
        SplashImageMode.CUSTOM_RANDOM -> {
          val imageList = MoegirlSplashImageManager.getImageList()
          SettingsStore.common.getValue { this.selectedSplashImages }
            .map { it.ifEmpty { imageList.map { it.key } } }
            .map { splashImageKeys -> imageList.filter { splashImageKeys.contains(it.key) } }
            .first()
            .randomOrNull() ?: SplashImage.onlyUseInSplashScreen(MoegirlSplashImageManager.fallbackImage)
        }
        else -> null
      }!!
    } else {
      HmoeSplashImageManager.getRandomImage()
    }
  }

  suspend fun showAppearAnimation() {
    imageScale.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = 2500,
      )
    )
  }
}