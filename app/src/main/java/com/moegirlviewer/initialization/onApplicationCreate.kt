package com.moegirlviewer.initialization

import android.app.Application
import com.moegirlviewer.component.articleView.util.LocalHttpServer
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.Globals
import com.tencent.smtt.sdk.QbSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Application.initializeOnCreate() {
  Globals.room = initRoom(applicationContext)
  coroutineScope.launch {
    LocalHttpServer.start()
    QbSdk.forceSysWebView()
  }
}

private val coroutineScope = CoroutineScope(Dispatchers.Default)