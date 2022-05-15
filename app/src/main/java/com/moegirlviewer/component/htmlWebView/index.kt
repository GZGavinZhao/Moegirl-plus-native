package com.moegirlviewer.component.htmlWebView

import android.annotation.SuppressLint
import android.view.View
import android.webkit.JavascriptInterface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.component.htmlWebView.utils.createHtmlDocument
import com.moegirlviewer.component.htmlWebView.utils.createWebViewTrackDrawable
import com.moegirlviewer.util.LocalCachedWebViews
import com.moegirlviewer.util.printDebugLog
import com.moegirlviewer.util.toUnicodeForInjectScriptInWebView
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val assetsBaseUrl = "file:///android_asset/"

private const val baseScript = """
  window._postMessage = function(type, data) {
    window._NativeInterface.postMessage(JSON.stringify({ type, data }))
  }
"""

typealias HtmlWebViewMessageHandlers = Map<String, (data: JsonObject?) -> Unit>
typealias HtmlWebViewScrollChangeHandler = (l: Int, t: Int, oldl: Int, oldt: Int) -> Unit

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlWebView(
  modifier: Modifier = Modifier,
  url: String? = null,  // 传入时，baseUrl无效
  baseUrl: String = assetsBaseUrl,
  messageHandlers: HtmlWebViewMessageHandlers? = null,
  ref: Ref<HtmlWebViewRef>,
  shouldInterceptRequest: ((webView: WebView, request: WebResourceRequest) -> WebResourceResponse?)? = null,
  onScrollChanged: HtmlWebViewScrollChangeHandler? = null
) {
  val isInDarkTheme = false
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()
  val content = rememberFromMemory("content") { Ref<HtmlWebViewContent>() }
  val webViewRef = remember { Ref<WebView>() }
  val cachedWebView = LocalCachedWebViews.current.singleCachedWebViewConsumer

  suspend fun injectScript(
    scriptContent: String
  ): String {
    return withContext(Dispatchers.Main) {
      val completableDeferred = CompletableDeferred<String>()
      webViewRef.value?.evaluateJavascript(scriptContent) { completableDeferred.complete(it) }
      completableDeferred.await()
    }
  }

  fun reloadWebView() {
    val contentValue = content.value!!

    val htmlDocument = if (contentValue.fullBody) {
      contentValue.body
    } else {
      createHtmlDocument(content.value!!.body,
        title = contentValue.title,
        injectedFiles = contentValue.injectedFiles ?: emptyList(),
        injectedStyles = listOf(
          *contentValue.injectedStyles?.toTypedArray() ?: emptyArray()
        ),
        injectedScripts = listOf(
          baseScript,
          *contentValue.injectedScripts?.toTypedArray() ?: emptyArray()
        )
      )
    }

    // html内容转unicode，防止误解析。因为计算量大这里使用协程
    scope.launch {
      withContext(Dispatchers.Default) {
        val willExecJavascript = """
          document.open()
          document.write("${htmlDocument.toUnicodeForInjectScriptInWebView()}")
          document.close()
          document.title = "${content.value!!.title}"
        """.trimIndent()
        injectScript(willExecJavascript)
      }
    }
  }

  fun updateContent(contentBuilder: (oldContent: HtmlWebViewContent) -> HtmlWebViewContent) {
    content.value = contentBuilder(content.value?.copy() ?: HtmlWebViewContent(""))
    reloadWebView()
  }

  SideEffect {
    ref.value = HtmlWebViewRef(
      updateContent = { updateContent(it) },
      reload = { reloadWebView() },
      injectScript = { injectScript(it) },
      webView = webViewRef.value!!
    )
  }

  LaunchedEffect(true) {
    val webView = webViewRef.value!!
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
    val trackDrawable = createWebViewTrackDrawable(density)
    webView.x5WebViewExtension?.setVerticalTrackDrawable(trackDrawable)
    webView.x5WebViewExtension?.setVerticalScrollBarDrawable(trackDrawable)

    with (webView.settings) {
      this.allowFileAccess = true
      this.allowContentAccess = true
      this.domStorageEnabled = true
      this.javaScriptEnabled = true
    }

    webView.webViewClient = object : WebViewClient() {
      override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return shouldInterceptRequest?.invoke(view, request)
      }
    }

    /*
    * 在全局命名空间声明了一个变量用来保存messageHandlersReferenceMap，在postMessage方法内直接引用。
    * 因为这里webview的实例已经委托给viewModel来保存，当取出来的时候，绑定的messageHandlers也还是之前的，
    * 这样就导致触发的handler全都是旧的。
    * 而重新removeJavascriptInterface & addJavascriptInterface也不可行，因为注入的接口必须重新加载页面后才能使用
    * 不得已只能使用这种的方式。
    * */
    messageHandlersReferenceMap[webView] = fun(dataStr: String) {
      val data = JsonParser.parseString(dataStr).asJsonObject
      val type = data.get("type").asString
      val typeData = data.get("data")?.asJsonObject
      messageHandlers?.let { it[type]?.invoke(typeData) }
    }

    webView.setOnScrollChangeListener { _, l, t, oldl, oldt ->
      onScrollChanged?.invoke(l, t, oldl, oldt)
    }

    if (!cachedWebView.existsInstance) {
      webView.addJavascriptInterface(object {
        @JavascriptInterface
        fun postMessage(data: String) {
          messageHandlersReferenceMap[webView]!!(data)
        }
      }, "_NativeInterface")

      if (url != null) {
        webView.loadUrl(url)
      } else {
        webView.loadDataWithBaseURL(baseUrl, "", "text/html", "utf8", null)
      }
      cachedWebView.webViewInstance = webView
    }
  }

  AndroidView(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier)
    ,
    factory = {
      (cachedWebView.webViewInstance ?: WebView(it)).apply { webViewRef.value = this }
    }
  )
}

class HtmlWebViewRef(
  val updateContent: (contentBuilder: (oldContent: HtmlWebViewContent) -> HtmlWebViewContent) -> Unit,
  val reload: () -> Unit,
  val webView: WebView,
  val injectScript: suspend (s: String) -> String,
)

data class HtmlWebViewContent(
  val body: String = "",
  val title: String? = null,
  val fullBody: Boolean = false,  // 如果为true，则完全使用body字段的内容作为webview的内容，忽略下面的所有参数
  val injectedStyles: List<String>? = null,
  val injectedScripts: List<String>? = null,
  val injectedFiles: List<String>? = null,
)

private val messageHandlersReferenceMap = mutableMapOf<WebView, ((dataStr: String) -> Unit)?>()