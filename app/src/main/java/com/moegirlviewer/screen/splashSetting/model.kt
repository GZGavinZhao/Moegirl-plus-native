package com.moegirlviewer.screen.splashSetting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.moegirlviewer.store.SplashImageMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashSettingScreenModel @Inject constructor() : ViewModel() {

  override fun onCleared() {
    super.onCleared()
  }
}