package com.moegirlviewer.util

import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val scope = CoroutineScope(Dispatchers.Main)

fun toast(text: String) {
  scope.launch {
    Toast.makeText(Globals.context, text, Toast.LENGTH_LONG).show()
  }
}