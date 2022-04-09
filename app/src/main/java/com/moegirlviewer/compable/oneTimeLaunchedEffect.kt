package com.moegirlviewer.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.moegirlviewer.util.InitRef
import kotlinx.coroutines.CoroutineScope

@Composable
fun OneTimeLaunchedEffect(
  vararg key: Any,
  block: suspend CoroutineScope.() -> Boolean,
) {
  val runFlag = remember { InitRef(false) }

  LaunchedEffect(*key) {
    if (!runFlag.value) {
      if (block()) runFlag.value = true
    }
  }
}