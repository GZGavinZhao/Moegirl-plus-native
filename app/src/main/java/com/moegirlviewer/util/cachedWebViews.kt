package com.moegirlviewer.util

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.staticCompositionLocalOf

private typealias CachedWebViewMapContainer = MutableMap<String, WebView>

val LocalCachedWebViews = staticCompositionLocalOf<CachedWebViewsConsumer> { error("LocalCachedWebViews缺少提供者！") }

class CachedWebViews {
  private val container: CachedWebViewMapContainer = mutableMapOf()
  private val consumer = CachedWebViewsConsumer(container)

  @Composable
  fun Provider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
      LocalCachedWebViews provides consumer
    ) {
      content()
    }
  }

  fun destroyAllInstance() {
    container.values.toList().forEach { it.destroy() }
  }
}


class CachedWebViewsConsumer(
  private val container: CachedWebViewMapContainer
) {
  // 如果页面中只使用一个webView，可以通过这个属性获取一个无需传入id的webView提供者
  val singleCachedWebViewConsumer: SingleCachedWebViewConsumer
    @Composable get() = SingleCachedWebViewConsumer(createId(), container)

  @Composable
  fun createId(key: String = "default"): String {
    return "CachedWebViewId-$key:$currentCompositeKeyHash"
  }

  fun takeInstance(cachedWebViewId: String): WebView? {
    return container[cachedWebViewId]
  }

  fun putInstance(cachedWebViewId: String, webView: WebView) {
    container[cachedWebViewId] = webView
  }

  fun hasInstance(cachedWebViewId: String): Boolean {
    return container.contains(cachedWebViewId)
  }
}

class SingleCachedWebViewConsumer(
  private val cachedWebViewId: String,
  private val container: CachedWebViewMapContainer
) {
  var webViewInstance: WebView?
    get() = container[cachedWebViewId]
    set(value) { container[cachedWebViewId] = value!! }

  val existsInstance: Boolean
    get() = container.contains(cachedWebViewId)
}