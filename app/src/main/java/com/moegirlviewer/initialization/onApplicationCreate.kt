package com.moegirlviewer.initialization

import android.app.Application
//import com.github.piasy.biv.BigImageViewer
//import com.github.piasy.biv.loader.fresco.FrescoImageLoader
import com.moegirlviewer.room.initRoom
import com.moegirlviewer.util.Globals

fun Application.initializeOnCreate() {
  Globals.room = initRoom(applicationContext)
}