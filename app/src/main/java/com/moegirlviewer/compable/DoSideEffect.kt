package com.moegirlviewer.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.moegirlviewer.util.InitRef

@Composable
fun DoSideEffect(
  effect: () -> Unit
) {
  val doneFlag = remember { InitRef(false) }

  if (!doneFlag.value) {
    effect()
    doneFlag.value = true
  }

  SideEffect(effect)
}