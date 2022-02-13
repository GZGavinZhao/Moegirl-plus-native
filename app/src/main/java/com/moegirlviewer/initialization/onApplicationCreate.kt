package com.moegirlviewer.initialization

import android.app.Application
import android.webkit.WebView
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.Globals

fun Application.initializeOnCreate() {
  Globals.room = initRoom(applicationContext)
//  LocalHttpServer.start()
}