package com.moegirlviewer.initialization

import android.app.Application
import com.moegirlviewer.component.articleView.util.LocalHttpServer
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.Globals
import com.tencent.smtt.sdk.QbSdk

fun Application.initializeOnCreate() {
  Globals.room = initRoom(applicationContext)
  LocalHttpServer.start()
  QbSdk.forceSysWebView()
}