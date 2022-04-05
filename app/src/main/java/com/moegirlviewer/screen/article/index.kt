package com.moegirlviewer.screen.article

import ArticleErrorMask
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.compable.FirstTimeSkippedLaunchedEffect
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.compable.remember.rememberDebouncedManualEffector
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.htmlWebView.HtmlWebViewScrollChangeHandler
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.article.component.ArticleLoadingMask
import com.moegirlviewer.screen.article.component.catalog.ArticleScreenCatalog
import com.moegirlviewer.screen.article.component.commentButton.CommentButton
import com.moegirlviewer.screen.article.component.findBar.ArticleScreenFindBar
import com.moegirlviewer.screen.article.component.header.ArticleScreenHeader
import com.moegirlviewer.screen.article.component.header.MoreMenuAction.*
import com.moegirlviewer.screen.drawer.CommonDrawer
import com.moegirlviewer.screen.pageRevisions.PageRevisionsRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@ExperimentalMaterialApi
@Composable
fun ArticleScreen(
  arguments: ArticleRouteArguments,
  ) {
  val model: ArticleScreenModel = hiltViewModel()

  SideEffect {
    model.routeArguments = arguments
  }

  LaunchedEffect(true) {
    model.visibleHeader = true
  }

  LaunchedEffect(true) {
    if (ArticleScreenModel.needReload) {
      ArticleScreenModel.needReload = false
      model.articleViewRef.value!!.reload(true)
    }
  }

  LaunchedEffect(true) {
    val isLoggedIn = AccountStore.isLoggedIn.first()
    val userInfo = AccountStore.userInfo.first()
    if (isLoggedIn && userInfo == null) {
      try {
        AccountStore.loadUserInfo()
      } catch(e: MoeRequestException) {
        printRequestErr(e, "获取用户信息失败")
      }
    }
    if (model.articleData != null) model.checkEditAllowed()
  }

  DisposableEffect(true) {
    // 这里必须用model的协程上下文，使用compose的协程会因为离开页面时compose的协程上下文会被销毁，导致onDispose的代码无法执行
    model.coroutineScope.launch {
      if (model.isMediaDisabled) {
        model.articleViewRef.value!!.enableAllMedia()
        model.isMediaDisabled = false
      }
    }

    onDispose {
      model.coroutineScope.launch {
        if (SettingsStore.common.getValue { this.stopMediaOnLeave }.first()) {
          model.articleViewRef.value!!.disableAllMedia()
        }
      }
    }
  }

  CommonDrawer {
    model.memoryStore.Provider {
      model.cachedWebViews.Provider {
        ArticleScreenCatalog(
          catalogData = model.catalogData,
          customDrawerState = model.catalogDrawerState,
          onSectionClick = { model.jumpToAnchor(it) }
        ) {
          Scaffold(
            modifier = Modifier
              .imeBottomPadding()
          ) {
            Box() {
              Box(
                modifier = Modifier
                  .absoluteOffset()
                  .zIndex(1f)
              ) {
                ComposedHeader()
              }

              ComposedArticleView(
                arguments = arguments
              )

              ArticleScreenFindBar(
                visible = model.visibleFindBar,
                onFindAll = { model.articleViewRef.value!!.htmlWebViewRef!!.webView.findAllAsync(it) },
                onFindNext = { model.articleViewRef.value!!.htmlWebViewRef!!.webView.findNext(true) },
                onClose = {
                  model.visibleFindBar = false
                  model.articleViewRef.value!!.htmlWebViewRef!!.webView.clearMatches()
                }
              )

              if (model.commentButtonAllowed && model.pageId != null) {
                CommentButton(
                  pageId = model.pageId!!,
                  visible = model.visibleCommentButton,
                  pageName = model.displayPageName
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ComposedHeader() {
  val model: ArticleScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  ArticleScreenHeader(
    title = model.displayPageName,
    visible = model.visibleHeader,
    onAction = {
      when(it) {
        REFRESH -> {
          scope.launch { model.articleViewRef.value!!.reload(true) }
        }
        GOTO_EDIT -> {
          scope.launch { model.handleOnGotoEditClicked() }
        }
        GOTO_LOGIN -> {
          Globals.navController.navigate("login")
        }
        TOGGLE_WATCH_LIST -> {
          scope.launch { model.togglePageIsInWatchList() }
        }
        SHOW_CATALOG -> {
          scope.launch {
            model.catalogDrawerState.open()
          }
        }
        SHARE -> { model.share() }
        GOTO_ADD_SECTION -> {
          scope.launch { model.handleOnAddSectionClicked() }
        }
        GOTO_TALK -> {
          scope.launch { model.handleOnGotoTalk() }
        }
        GOTO_PAGE_REVISIONS -> {
          Globals.navController.navigate(PageRevisionsRouteArguments(
            pageName = model.truePageName!!
          ))
        }
        SHOW_FIND_BAR -> {
          model.visibleFindBar = true
        }
      }
    }
  )
}

@Composable
private fun ComposedArticleView(
  arguments: ArticleRouteArguments,
) {
  val model: ArticleScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  val debouncedManualEffector = rememberDebouncedManualEffector<ReadingRecord>(1000) {
    scope.launch {
      SettingsStore.other.setValue {
        this.readingRecord = it
      }
    }
  }

  val oldScrollValue = rememberSaveable { mutableListOf(0) }
  FirstTimeSkippedLaunchedEffect(model.scrollState.value) {
    if (model.articleLoadStatus != LoadStatus.SUCCESS) return@FirstTimeSkippedLaunchedEffect

    val oldValue = oldScrollValue.first()
    val value = model.scrollState.value

    model.visibleHeader = value < 80 || value < oldValue
    model.visibleCommentButton = value < oldValue
    scope.launch {
      debouncedManualEffector.trigger(ReadingRecord(
        pageName = model.truePageName!!,
        progress = value.toFloat() / model.articleViewRef.value!!.htmlWebViewRef!!.webView.contentHeight,
        scrollY = value
      ))
    }

    oldScrollValue[0] = value
  }

//  val handleOnScrollChanged: HtmlWebViewScrollChangeHandler = { _, top, _, oldTop ->
//    model.visibleHeader = top < 80 || top < oldTop
//    model.visibleCommentButton = top < oldTop
//    scope.launch {
//      debouncedManualEffector.trigger(ReadingRecord(
//        pageName = model.truePageName!!,
//        progress = top.toFloat() / model.articleViewRef.value!!.htmlWebViewRef!!.webView.contentHeight,
//        scrollY = top
//      ))
//    }
//  }

  LaunchedEffect(model.articleLoadStatus) {
    model.swipeRefreshState.isRefreshing = model.articleLoadStatus == LoadStatus.LOADING
  }

  SwipeRefresh(
    state = model.swipeRefreshState,
    onRefresh = {
      scope.launch { model.articleViewRef.value!!.reload(true) }
    },
    indicator = { state, refreshTriggerDistance ->
      val headerHeight = Globals.statusBarHeight + Constants.topAppBarHeight

      if (model.scrollState.value == 0) {
        StyledSwipeRefreshIndicator(
          modifier = Modifier
            .padding(top = headerHeight.dp),
          state = state,
          refreshTriggerDistance = refreshTriggerDistance
        )
      }
    }
  ) {
    Center {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(model.scrollState),
        contentAlignment = Alignment.Center
      ) {
        ArticleView(
          props = ArticleViewProps(
            pageName = arguments.pageName,
            pageId = arguments.pageId,
            revId = arguments.revId,
            editAllowed = model.editAllowed ?: false,
            visibleLoadStatusIndicator = false,
            contentTopPadding = (Constants.topAppBarHeight + Globals.statusBarHeight).dp,
            addCategories = model.truePageName != "H萌娘:官方群组",
//            onScrollChanged = handleOnScrollChanged,
            onArticleLoaded = { data, info -> model.handleOnArticleLoaded(data, info) },
            onArticleRendered = { model.handleOnArticleRendered() },
            onArticleMissed = { model.handleOnArticleMissed() },
            onStatusChanged = { model.articleLoadStatus = it },
            emitCatalogData = { model.catalogData = it },
            ref = model.articleViewRef,
          )
        )
      }

      AnimatedVisibility(
        visible = model.articleLoadStatus == LoadStatus.LOADING,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        ArticleLoadingMask()
      }

      AnimatedVisibility(
        visible = model.articleLoadStatus == LoadStatus.FAIL,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        ArticleErrorMask(
          onClick = {
            scope.launch { model.articleViewRef.value!!.reload(true) }
          }
        )
      }
    }
  }

}

class ReadingRecord(
  val pageName: String,
  val progress: Float,
  val scrollY: Int,
  val date: LocalDateTime = LocalDateTime.now()
)