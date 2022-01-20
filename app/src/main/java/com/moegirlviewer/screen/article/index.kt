package com.moegirlviewer.screen.article

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.htmlWebView.HtmlWebViewScrollChangeHandler
import com.moegirlviewer.screen.article.component.catalog.ArticleScreenCatalog
import com.moegirlviewer.screen.article.component.commentButton.CommentButton
import com.moegirlviewer.screen.article.component.findBar.ArticleScreenFindBar
import com.moegirlviewer.screen.article.component.header.ArticleScreenHeader
import com.moegirlviewer.screen.article.component.header.MoreMenuAction.*
import com.moegirlviewer.screen.drawer.CommonDrawer
import com.moegirlviewer.screen.pageRevisions.PageRevisionsRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.LoadUserInfoException
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.imeBottomPadding
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.printRequestErr
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun ArticleScreen(
  arguments: ArticleRouteArguments,
  ) {
  val scope = rememberCoroutineScope()
  val model: ArticleScreenModel = hiltViewModel()
  val statusBarHeight = Globals.statusBarHeight
  val catalogRef = Ref<CustomDrawerRef>()

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
      } catch(e: LoadUserInfoException) {
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

  val handleOnScrollChanged: HtmlWebViewScrollChangeHandler = { _, top, _, oldTop ->
    model.visibleHeader = top < 80 || top < oldTop
    model.visibleCommentButton = top < oldTop
  }

  StatusBar(
    darkIcons = false
  )

  CommonDrawer {
    model.memoryStore.Provider {
      model.cachedWebViews.Provider {
        ArticleScreenCatalog(
          catalogData = model.catalogData,
          ref = catalogRef,
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
                        catalogRef.value!!.open()
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

              ArticleView(
                props = ArticleViewProps(
                  pageName = arguments.pageName,
                  pageId = arguments.pageId,
                  revId = arguments.revId,
                  editAllowed = model.editAllowed ?: false,
                  contentTopPadding = (Constants.topAppBarHeight + statusBarHeight).dp,
                  onScrollChanged = handleOnScrollChanged,
                  onArticleLoaded = { data, info -> model.handleOnArticleLoaded(data, info) },
                  onArticleRendered = { model.handleOnArticleRendered() },
                  onArticleMissed = { model.handleOnArticleMissed() },
                  emitCatalogData = { model.catalogData = it },
                  ref = model.articleViewRef,
                )
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

