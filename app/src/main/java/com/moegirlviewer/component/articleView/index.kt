package com.moegirlviewer.component.articleView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moegirlviewer.Constants
import com.moegirlviewer.api.page.bean.PageContentResBean
import com.moegirlviewer.api.page.bean.PageInfoResBean
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.htmlWebView.HtmlWebView
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.component.htmlWebView.HtmlWebViewScrollChangeHandler
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.screen.article.ReadingRecord
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch
import kotlin.math.max

typealias ArticleData = PageContentResBean
typealias ArticleInfo = PageInfoResBean.Query.MapValue

class ArticleViewProps(
  val modifier: Modifier = Modifier,
  val pageName: String? = null,   // 不传html时，pageName和pageId之中必须传一个
  val pageId: Int? = null,
  val html: String? = null,
  val revId: Int? = null,
  val readingRecord: ReadingRecord? = null,
  val injectedStyles: List<String>? = null,
  val injectedScripts: List<String>? = null,
  val visibleLoadStatusIndicator: Boolean = true,
  val linkDisabled: Boolean = false,
  val fullHeight: Boolean = false,  // 用于外部容器代理滚动的模式
  val inDialogMode: Boolean = false,
  val editAllowed: Boolean = false,
  val addCopyright: Boolean = false,
  val addCategories: Boolean = true,
  val cacheEnabled: Boolean = false,
  val contentTopPadding: Dp = 0.dp,
  val renderDelay: Long = 0,
  val messageHandlers: HtmlWebViewMessageHandlers? = null,

  val emitCatalogData: ((data: List<ArticleCatalog>) -> Unit)? = null,

  val onScrollChanged: HtmlWebViewScrollChangeHandler? = null,
  val onArticleRendered: (() -> Unit)? = null,
  val onArticleLoaded: ((articleData: ArticleData, articleInfo: ArticleInfo) -> Unit)? = null,
  val onArticleMissed: (() -> Unit)? = null,
  val onArticleError: (() -> Unit)? = null,
  val onStatusChanged: ((LoadStatus) -> Unit)? = null,

  val ref: Ref<ArticleViewRef>? = null
)

class ArticleViewRef(
  val loadStatus: LoadStatus,
  val reload: suspend (force: Boolean) -> Unit,
  val updateView: suspend () -> Unit,
  val htmlWebViewRef: HtmlWebViewRef?,
  val enableAllMedia: suspend () -> Unit,
  val disableAllMedia: suspend () -> Unit,
)

@Composable
fun ArticleView(
  props: ArticleViewProps
) {
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val state = ArticleViewState.remember(props)

  SideEffect {
    props.ref?.value = ArticleViewRef(
      loadStatus = state.status,
      reload = { state.loadArticleContent(forceLoad = it) },
      updateView = { state.updateHtmlView(true) },
      htmlWebViewRef = state.htmlWebViewRef.value,
      enableAllMedia = { state.enableAllMedia() },
      disableAllMedia = { state.disableAllMedia() },
    )
  }

  LaunchedEffect(state.status) {
    props.onStatusChanged?.invoke(state.status)
  }

  // 这段逻辑只能用来初始化，初始化之后再要更新需要手动调用loadArticleContent或updateHtmlView
  LaunchedEffect(
    props.pageName,
    props.pageId,
    props.revId,
    props.html
  ) {
    if (state.status == LoadStatus.LOADING || state.status == LoadStatus.INITIAL) {
      if (props.html.isNullOrEmpty()) {
        if (props.pageName != null || props.pageId != null || props.revId != null) state.loadArticleContent()
      } else {
        state.updateHtmlView()
      }
    }
  }

  LaunchedEffect(true) {
    state.checkUserConfig()
  }

  LaunchedEffect(themeColors.isLight) {
    if (state.status == LoadStatus.SUCCESS) {
      state.htmlWebViewRef.value!!.injectScript("""
        moegirl.config.nightTheme.${'$'}enabled = ${!themeColors.isLight}
        document.querySelector('html').style.cssText = `
          --color-primary: ${themeColors.primaryVariant.toCssRgbaString()};
          --color-dark: ${themeColors.primaryVariant.darken(0.3F).toCssRgbaString()};
          --color-light: ${themeColors.primaryVariant.lighten(0.3F).toCssRgbaString()};
        `
        ${if (props.inDialogMode && !themeColors.isLight)
          "document.body.style.backgroundColor = '${themeColors.surface.toCssRgbaString()}'"
        else ""}
      """.trimIndent())
    }
  }

  fun reloadContent() = scope.launch {
    state.loadArticleContent(forceLoad = true)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .then(if (props.fullHeight) Modifier.height(state.contentHeight.dp) else Modifier.fillMaxHeight())
  ) {
    HtmlWebView(
      messageHandlers = state.defaultMessageHandlers + (props.messageHandlers ?: emptyMap()),
      onScrollChanged = props.onScrollChanged,
      ref = state.htmlWebViewRef,
      shouldInterceptRequest = { webView, request -> state.shouldInterceptRequest(webView, request) }
    )

    if (props.visibleLoadStatusIndicator && state.status != LoadStatus.SUCCESS) {
      Box(
        modifier = Modifier
          .noRippleClickable { }
          .absoluteOffset(0.dp, 0.dp)
          .matchParentSize()
          .background(themeColors.background)
          .padding(top = props.contentTopPadding),
        contentAlignment = Alignment.Center
      ) {
        if (state.status == LoadStatus.LOADING) StyledCircularProgressIndicator()
        if (state.status == LoadStatus.FAIL) {
          ReloadButton(
            modifier = Modifier
              .matchParentSize(),
            onClick = { reloadContent() }
          )
        }
      }
    }
  }
}

@ProguardIgnore
class ArticleCatalog(
  val level: Int,
  val id: String,
  val name: String
)