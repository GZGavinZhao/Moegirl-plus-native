package com.moegirlviewer.screen.splashSetting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.util.MoegirlSplashImageManager
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashSettingScreenModel @Inject constructor() : ViewModel() {
  var isImageSyncing by mutableStateOf(false)

  suspend fun syncSplashImages() {
    try {
      isImageSyncing = true
      MoegirlSplashImageManager.loadConfig()
      MoegirlSplashImageManager.syncImagesByConfig()
      MoegirlSplashImageManager.checkImageSyncStatus()
      if (MoegirlSplashImageManager.imageTotal != 0) {
        toast("启动屏图集同步成功")
      } else {
        throw Exception()
      }
    } catch (e: Exception) {
      toast("同步启动屏图集发生错误")
    } finally {
      isImageSyncing = false
    }
  }

  override fun onCleared() {
    super.onCleared()
  }
}