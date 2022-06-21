package com.moegirlviewer.compable

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.moegirlviewer.util.InitRef
import kotlinx.coroutines.CoroutineScope

@Composable
fun OneTimeLaunchedEffect(
  vararg key: Any,
  block: suspend CoroutineScope.() -> Boolean,
) {
  var runFlag by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(*key) {
    if (!runFlag) {
      if (block()) runFlag = true
    }
  }
}