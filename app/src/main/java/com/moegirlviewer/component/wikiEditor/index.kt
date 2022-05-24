package com.moegirlviewer.component.wikiEditor

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import com.google.gson.Gson
import com.moegirlviewer.component.htmlWebView.HtmlWebView
import com.moegirlviewer.component.htmlWebView.HtmlWebViewContent
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.theme.TextColors
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.ProguardIgnore
import com.moegirlviewer.util.toCssRgbaString
import com.moegirlviewer.util.toUnicodeForInjectScriptInWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WikiEditor(
  modifier: Modifier = Modifier,
  state: WikiEditorState,
  onTextChange: ((String) -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val themeTextColors = themeColors.text
  val scope = rememberCoroutineScope()

  LaunchedEffect(true) {
    if (!state.isInitialized) {
      state.init(themeColors, themeTextColors)
    }
  }

  LaunchedEffect(themeColors.isLight) {
    state.htmlWebViewRef.value!!.injectScript("""
        const element = document.querySelector('.CodeMirror')
        if (element) element.style.color = '${themeTextColors.primary.toCssRgbaString()}'
      """.trimIndent())
  }

  HtmlWebView(
    modifier = modifier,
    ref = state.htmlWebViewRef,
    messageHandlers = mapOf(
      "onLoaded" to {
        scope.launch {
          state.setTextContent(state.lastSettingContent)
        }
      },

      "onTextChange" to {
        val textContent = it!!.get("text").asString
        onTextChange?.invoke(textContent)
      }
    ),
    onPageFinished = {
      if (!state.isInitialized) state.init(themeColors, themeTextColors)
    }
  )
}

class WikiEditorState {
  internal val htmlWebViewRef = Ref<HtmlWebViewRef>()
  internal var lastSettingContent = ""
  var isInitialized = false

  internal fun init(themeColors: Colors, themeTextColors: TextColors) {
    val style = """
      .CodeMirror-line::selection, 
      .CodeMirror-line>span::selection, 
      .CodeMirror-line>span>span::selection {
        background-color: ${themeColors.primaryVariant.copy(alpha = 0.3f).toCssRgbaString()} !important;
      }

      /* 这段代码不生效，经过测试CodeMirror会检测是否为移动端，如果是的话就不会使用自定义的光标。可能是使用自定义光标会有问题，但这里还是姑且保留一下 */
      .CodeMirror-cursor {
        border-left: 2px solid ${themeColors.primaryVariant.toCssRgbaString()}     
      }
      
      body {
        caret-color: ${themeColors.primaryVariant.toCssRgbaString()};
        font-size: 16px;
      }
      
      .CodeMirror {
        color: ${themeTextColors.primary.toCssRgbaString()}
      }
    """.trimIndent()

    val script = """
      window.onEditorTextChange = text => _postMessage('onTextChange', { text })
      _postMessage('onLoaded')
    """.trimIndent()

    isInitialized = true
    htmlWebViewRef.value!!.updateContent {
      HtmlWebViewContent(
        title = "wikiEditor",
        injectedStyles = listOf(style),
        injectedScripts = listOf(script),
        injectedFiles = listOf(
          "editor-main.js",
          "editor-main.css"
        )
      )
    }
  }

  internal suspend fun getTextContent(): String {
    val rawResult = htmlWebViewRef.value!!.injectScript("editor.getValue()")
    return Gson().fromJson(rawResult, String::class.java)
  }

  internal suspend fun setTextContent(text: String) {
    lastSettingContent = text
    val escapedText = withContext(Dispatchers.Default) { text.toUnicodeForInjectScriptInWebView() }
    htmlWebViewRef.value!!.injectScript("editor.setValue('$escapedText')")
  }

  internal suspend fun insertTextAtCursor(text: String) {
    val escapedText = withContext(Dispatchers.Default) { text.toUnicodeForInjectScriptInWebView() }
    htmlWebViewRef.value!!.injectScript("editor.replaceSelection('$escapedText')")
  }

  suspend fun getPosition(): WikiEditorCursorPosition {
    val positionJson = htmlWebViewRef.value!!.injectScript("editor.getCursor()")
    if (positionJson == "null") return WikiEditorCursorPosition.Zero
    return Gson().fromJson(positionJson, WikiEditorCursorPosition::class.java)
  }

  suspend fun setSelection(
    startPosition: WikiEditorCursorPosition,
    endPosition: WikiEditorCursorPosition
  ) {
    htmlWebViewRef.value!!.injectScript("""
      editor.setSelection(
        { line: ${startPosition.line}, ch: ${startPosition.ch} },
        { line: ${endPosition.line}, ch: ${endPosition.ch} },
      )
    """.trimIndent())
  }

  suspend fun setCursorPosition(position: WikiEditorCursorPosition) {
    setSelection(position, position)
  }

  suspend fun setBottomPadding(value: Int) {
    htmlWebViewRef.value!!.injectScript("""(() => {
      const width = document.documentElement.clientWidth
      const height = document.documentElement.clientHeight - $value
      window.editor.setSize(width, height)  
      document.body.style.paddingBottom = $value + 'px'
    })()""".trimIndent())
  }
}

@ProguardIgnore
data class WikiEditorCursorPosition(
  val line: Int,
  val ch: Int
) {
  companion object {
    val Zero = WikiEditorCursorPosition(0, 0)
  }
}