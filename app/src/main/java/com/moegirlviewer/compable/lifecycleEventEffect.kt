package com.moegirlviewer.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun LifecycleEventEffect(
  onResume: (() -> Unit)? = null,
) {
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when(event) {
        Lifecycle.Event.ON_RESUME -> onResume?.invoke()
        else -> {}
      //        Lifecycle.Event.ON_CREATE -> TODO()
//        Lifecycle.Event.ON_START -> TODO()
//        Lifecycle.Event.ON_PAUSE -> TODO()
//        Lifecycle.Event.ON_STOP -> TODO()
//        Lifecycle.Event.ON_DESTROY -> TODO()
//        Lifecycle.Event.ON_ANY -> TODO()
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}