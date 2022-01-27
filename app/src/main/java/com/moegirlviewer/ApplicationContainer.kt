package com.moegirlviewer

import android.app.Application
import android.util.Log
import com.moegirlviewer.initialization.initializeOnCreate
import dagger.hilt.android.HiltAndroidApp
import java.util.*


@HiltAndroidApp
class ApplicationContainer : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeOnCreate()
  }
}

