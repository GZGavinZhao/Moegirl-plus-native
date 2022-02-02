package com.moegirlviewer.compable.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun <T> rememberDebouncedManualEffector(
  delayMs: Long,
  effect: (T) -> Unit
): DebouncedManualEffector<T> {
  val mutableStateFlow = remember { MutableStateFlow<Any>(EmptyValue) }

  LaunchedEffect(true) {
    mutableStateFlow
      .debounce(delayMs)
      .collect {
        if (it != EmptyValue) effect(it as T)
      }
  }

  return remember { DebouncedManualEffector(mutableStateFlow) }
}

private object EmptyValue

class DebouncedManualEffector<T>(
  private val mutableStateFlow: MutableStateFlow<Any>
) {
  suspend fun trigger(value: T) {
    mutableStateFlow.emit(value as Any)
  }
}