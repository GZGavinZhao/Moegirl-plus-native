package com.moegirlviewer.initialization

import android.app.Application
import android.os.Bundle
import com.moegirlviewer.BuildConfig
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.Globals
import com.tencent.smtt.sdk.QbSdk
import com.uc.crashsdk.export.CrashApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun Application.initializeOnCreate() {
  Globals.room = initRoom(applicationContext)
  coroutineScope.launch {
//    LocalHttpServer.start()
    QbSdk.forceSysWebView()
  }

  if (!BuildConfig.DEBUG) {
//    val args = Bundle()
//    args.putBoolean("mDebug", true)
    CrashApi.createInstanceEx(applicationContext, "jks0183i-2qdc35hf", false)
  }
}

private val coroutineScope = CoroutineScope(Dispatchers.Default)