package com.moegirlviewer.util

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

// 如果flow内的值是复杂对象，如果对象内某些值还复用之前的，那么即使值更新了，也只有使用了collectAsState的组件会更新，
// 使用旧值组件不会更新，这个函数用于解决这个问题
@Composable
fun <T, S> Flow<T>.selector(
  initialFlowValue: T,
  selector: (flowValue: T) -> S,
  shouldUpdate: ((a: S, b: S) -> Boolean) = { a, b -> a != b }
): S {
  var value by remember { mutableStateOf(selector(initialFlowValue)) }

  LaunchedEffect(true) {
    this@selector.collect {
      val newValue = selector(it)
      if (shouldUpdate(value, newValue)) value = newValue
    }
  }

  return value
}