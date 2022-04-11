package com.moegirlviewer.initialization

import android.app.Application
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.Globals
import com.tencent.smtt.sdk.QbSdk.initX5Environment

fun Application.initializeOnCreate() {
  Globals.room = initRoom(applicationContext)
  initX5Environment(applicationContext, null)
}