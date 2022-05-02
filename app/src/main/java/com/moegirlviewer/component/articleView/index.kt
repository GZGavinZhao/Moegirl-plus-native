package com.moegirlviewer.component.articleView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moegirlviewer.compable.DoSideEffect
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.htmlWebView.HtmlWebView
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.htmlWebView.HtmlWebViewScrollChangeHandler
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.screen.article.ReadingRecord
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun ArticleView(
  modifier: Modifier = Modifier,
  state: ArticleViewState = ArticleViewState(),
  pageKey: PageKey? = null, // 不传html时，pageKey必传
  html: String? = null,
  revId: Int? = null,
  readingRecord: ReadingRecord? = null,
  injectedStyles: List<String>? = null,
  injectedScripts: List<String>? = null,
  visibleLoadStatusIndicator: Boolean = true,
  linkDisabled: Boolean = false,
  fullHeight: Boolean = false,  // 用于外部容器代理滚动的模式
  inDialogMode: Boolean = false,
  editAllowed: Boolean = false,
  addCopyright: Boolean = false,
  addCategories: Boolean = true,
  cacheEnabled: Boolean = false,
  previewMode: Boolean = false,   // 这个参数对应的就是api的preview参数，没有其他功能，使用这个会获得不带缓存的渲染结果
  visibleEditButton: Boolean = true,
  contentTopPadding: Dp = 0.dp,
  renderDelay: Long = 0,
  messageHandlers: HtmlWebViewMessageHandlers? = null,
  emitCatalogData: ((data: List<ArticleCatalog>) -> Unit)? = null,
  onArticleLoaded: ((articleData: ArticleData, articleInfo: ArticleInfo?) -> Unit)? = null,
  onScrollChanged: HtmlWebViewScrollChangeHandler? = null,
  onArticleRendered: (() -> Unit)? = null,
  onArticleMissed: (() -> Unit)? = null,
  onArticleError: (() -> Unit)? = null,
) {
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val coreState = state.core

  DoSideEffect {
    coreState.pageKey = pageKey
    coreState.html = html
    coreState.revId = revId
    coreState.readingRecord = readingRecord
    coreState.injectedStyles = injectedStyles
    coreState.injectedScripts = injectedScripts
    coreState.visibleLoadStatusIndicator = visibleLoadStatusIndicator
    coreState.linkDisabled = linkDisabled
    coreState.fullHeight = fullHeight
    coreState.inDialogMode = inDialogMode
    coreState.editAllowed = editAllowed
    coreState.addCopyright = addCopyright
    coreState.addCategories = addCategories
    coreState.cacheEnabled = cacheEnabled
    coreState.previewMode = previewMode
    coreState.visibleEditButton = visibleEditButton
    coreState.contentTopPadding = contentTopPadding
    coreState.renderDelay = renderDelay
    coreState.messageHandlers = messageHandlers
    coreState.emitCatalogData = emitCatalogData
    coreState.onArticleLoaded = onArticleLoaded
    coreState.onScrollChanged = onScrollChanged
    coreState.onArticleRendered = onArticleRendered
    coreState.onArticleMissed = onArticleMissed
    coreState.onArticleError = onArticleError
  }

  // 这段逻辑只能用来初始化，初始化之后再要更新需要手动调用loadArticleContent或updateHtmlView
  LaunchedEffect(
    coreState.pageKey,
    coreState.revId,
    coreState.html
  ) {
    if (coreState.status == LoadStatus.LOADING || coreState.status == LoadStatus.INITIAL) {
      if (coreState.html.isNullOrEmpty()) {
        if (coreState.pageKey != null || coreState.revId != null) coreState.coroutineScope.launch { coreState.loadArticleContent() }
      } else {
        coreState.updateHtmlView()
      }
    }
  }

  LaunchedEffect(true) {
    state.core.checkUserConfig()
  }

  LaunchedEffect(themeColors.isLight) {
    if (coreState.status == LoadStatus.SUCCESS) {
      coreState.htmlWebViewRef.value!!.injectScript("""
        moegirl.config.nightTheme.${'$'}enabled = ${!themeColors.isLight}
        document.querySelector('html').style.cssText = `
          --color-primary: ${themeColors.primaryVariant.toCssRgbaString()};
          --color-dark: ${themeColors.primaryVariant.darken(0.3F).toCssRgbaString()};
          --color-light: ${themeColors.primaryVariant.lighten(0.3F).toCssRgbaString()};
        `
        ${if (coreState.inDialogMode && !themeColors.isLight)
        "document.body.style.backgroundColor = '${themeColors.surface.toCssRgbaString()}'"
      else ""}
      """.trimIndent())
    }
  }

  fun reloadContent() = scope.launch {
    coreState.loadArticleContent(forceLoad = true)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .then(if (coreState.fullHeight) Modifier.height(coreState.contentHeight.dp) else Modifier.fillMaxHeight())
      .then(modifier)
  ) {
    HtmlWebView(
      messageHandlers = coreState.defaultMessageHandlers + (coreState.messageHandlers ?: emptyMap()),
      onScrollChanged = coreState.onScrollChanged,
      ref = coreState.htmlWebViewRef,
      shouldInterceptRequest = { webView, request -> coreState.shouldInterceptRequest(webView, request) }
    )

    if (coreState.visibleLoadStatusIndicator && state.status != LoadStatus.SUCCESS) {
      Box(
        modifier = Modifier
          .noRippleClickable { }
          .absoluteOffset(0.dp, 0.dp)
          .matchParentSize()
          .background(themeColors.background)
          .padding(top = coreState.contentTopPadding),
        contentAlignment = Alignment.Center
      ) {
        if (coreState.status == LoadStatus.LOADING) StyledCircularProgressIndicator()
        if (coreState.status == LoadStatus.FAIL) {
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