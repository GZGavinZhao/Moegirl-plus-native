package com.moegirlviewer

import android.app.Application
import androidx.compose.material.Text
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.initialization.initializeOnCreate
import com.moegirlviewer.store.AccountStore
import dagger.hilt.android.HiltAndroidApp
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import refreshWatchList
import java.util.*


@HiltAndroidApp
class ApplicationContainer : Application() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)

  override fun onCreate() {
    super.onCreate()
    initializeOnCreate()
  }
}

