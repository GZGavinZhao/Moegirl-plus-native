package com.moegirlviewer.util

import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val scope = CoroutineScope(Dispatchers.Main)
private var queueCount = 0
private var lastSentText = ""

fun toast(text: String) {
  scope.launch {
    if (queueCount >= 3 || (queueCount != 0 && lastSentText == text)) return@launch
    Toast.makeText(Globals.context, text, Toast.LENGTH_LONG).show()
    lastSentText = text
    queueCount++
    delay(3000)
    queueCount--
  }
}