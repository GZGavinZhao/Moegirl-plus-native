package com.moegirlviewer.component.articleView

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.api.page.bean.PageContentResBean
import com.moegirlviewer.api.page.bean.PageInfoResBean
import com.moegirlviewer.component.htmlWebView.HtmlWebView
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.component.htmlWebView.HtmlWebViewScrollChangeHandler
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.ProguardIgnore
import com.moegirlviewer.util.noRippleClickable
import kotlinx.coroutines.launch

typealias ArticleData = PageContentResBean
typealias ArticleInfo = PageInfoResBean.Query.MapValue

class ArticleViewProps(
  val modifier: Modifier = Modifier,
  val pageName: String? = null,
  val pageId: Int? = null,
  val html: String? = null,
  val revId: Int? = null,
  val injectedStyles: List<String>? = null,
  val injectedScripts: List<String>? = null,
  val linkDisabled: Boolean = false,
  val fullHeight: Boolean = false,  // 用于外部容器代理滚动的模式
  val inDialogMode: Boolean = false,
  val editAllowed: Boolean = false,
  val addCopyright: Boolean = false,
  val addCategories: Boolean = true,
  val contentTopPadding: Dp = 0.dp,
  val renderDelay: Long = 0,
  val messageHandlers: HtmlWebViewMessageHandlers? = null,

  val emitCatalogData: ((data: List<ArticleCatalog>) -> Unit)? = null,

  val onScrollChanged: HtmlWebViewScrollChangeHandler? = null,
  val onArticleRendered: (() -> Unit)? = null,
  val onArticleLoaded: ((articleData: ArticleData, articleInfo: ArticleInfo) -> Unit)? = null,
  val onArticleMissed: (() -> Unit)? = null,
  val onArticleError: (() -> Unit)? = null,

  val ref: Ref<ArticleViewRef>? = null
)

class ArticleViewRef(
  val restoredStatus: LoadStatus,
  val reload: suspend (force: Boolean) -> Unit,
  val updateView: suspend () -> Unit,
  val htmlWebViewRef: HtmlWebViewRef?,
  val enableAllMedia: suspend () -> Unit,
  val disableAllMedia: suspend () -> Unit
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
      restoredStatus = state.status,
      reload = { state.loadArticleContent(forceLoad = it) },
      updateView = { state.updateHtmlView(true) },
      htmlWebViewRef = state.htmlWebViewRef.value,
      enableAllMedia = { state.enableAllMedia() },
      disableAllMedia = { state.disableAllMedia() }
    )
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

  fun reloadContent() = scope.launch {
    state.loadArticleContent(forceLoad = true)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    HtmlWebView(
      messageHandlers = state.defaultMessageHandlers + (props.messageHandlers ?: emptyMap()),
      onScrollChanged = props.onScrollChanged,
      ref = state.htmlWebViewRef
    )

    if (state.status != LoadStatus.SUCCESS) {
      Box(
        modifier = Modifier
          .noRippleClickable {  }
          .absoluteOffset(0.dp, 0.dp)
          .matchParentSize()
          .background(themeColors.background)
          .padding(top = props.contentTopPadding),
        contentAlignment = Alignment.Center
      ) {
        if (state.status == LoadStatus.LOADING) StyledCircularProgressIndicator()
        if (state.status == LoadStatus.FAIL) {
          TextButton(
            border = BorderStroke(0.dp, Color.Transparent),
            onClick = { reloadContent() }
          ) {
            Text(
              text = stringResource(R.string.reload),
              fontSize = 15.sp
            )
          }
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