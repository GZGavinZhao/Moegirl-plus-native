package com.moegirlviewer.compable.remember

import androidx.compose.runtime.*


@Composable
inline fun <T> rememberFromMemory(
  key: String,
  vararg args: Any?,
  calculation: @DisallowComposableCalls () -> T
): T {
  val memoryStore = LocalMemoryStore.current
  val stateKey = currentCompositeKeyHash.toString() + key
  var unrestored by remember { mutableStateOf(true) }

  return remember(*args) {
    if (unrestored && memoryStore.containsKey(stateKey)) return@remember memoryStore[stateKey] as T
    memoryStore[stateKey] = calculation()
    unrestored = false
    memoryStore[stateKey] as T
  }
}

class MemoryStore {
  private val store: StoreMap = mutableMapOf()

  @Composable
  fun Provider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
      LocalMemoryStore provides store
    ) {
      content()
    }
  }
}

private typealias StoreMap = MutableMap<String, Any?>

val LocalMemoryStore = staticCompositionLocalOf<StoreMap> { error("memory没有提供者！") }