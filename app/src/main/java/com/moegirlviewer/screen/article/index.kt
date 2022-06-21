package com.moegirlviewer.screen.article

import ArticleErrorMask
import com.moegirlviewer.component.articleView.ArticleView
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.CrossWikiUrlPrefix
import com.moegirlviewer.R
import com.moegirlviewer.compable.OneTimeLaunchedEffect
import com.moegirlviewer.compable.remember.rememberDebouncedManualEffector
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.htmlWebView.HtmlWebViewScrollChangeHandler
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@Composable
fun ArticleScreen(
  arguments: ArticleRouteArguments,
  ) {
  val model: ArticleScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
//  val isLightRequestMode by SettingsStore.common.getValue { lightRequestMode }.collectAsState(initial = false)
  val isLightRequestMode = true

  SideEffect {
    model.routeArguments = arguments
  }

  OneTimeLaunchedEffect(model.articleViewState.status) {
    if (model.articleViewState.status == LoadStatus.SUCCESS) {
      model.handleOnArticleLoaded()
      return@OneTimeLaunchedEffect true
    }

    false
  }

  LaunchedEffect(isLightRequestMode) {
    if (model.isLightRequestModeWhenOpened == true && !isLightRequestMode) {
      model.articleViewState.reload()
    }

    model.isLightRequestModeWhenOpened = isLightRequestMode
  }

  LaunchedEffect(true) {
    model.visibleHeader = true
  }

  LaunchedEffect(true) {
    if (ArticleScreenModel.needReload) {
      ArticleScreenModel.needReload = false
      model.articleViewState.reload()
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
        model.articleViewState.enableAllMedia()
        model.isMediaDisabled = false
      }
    }

    onDispose {
      model.coroutineScope.launch {
        if (SettingsStore.common.getValue { this.stopMediaOnLeave }.first()) {
          model.articleViewState.disableAllMedia()
        }
      }
    }
  }

  BackHandler(arguments.deepLinkMode) {
    Globals.activity.finishAndRemoveTask()
  }

  CommonDrawer(
    state = model.commonDrawerState
  ) {
    model.memoryStore.Provider {
      model.cachedWebViews.Provider {
        ArticleScreenCatalog(
          catalogData = model.catalogData,
          customDrawerState = model.catalogDrawerState,
          onSectionClick = {
            scope.launch { model.jumpToAnchor(it) }
          }
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
                ComposedHeader(
                  deepLinkMode = arguments.deepLinkMode
                )
              }

              ComposedArticleView(
                arguments = arguments
              )

              ArticleScreenFindBar(
                visible = model.visibleFindBar,
                onFindAll = { model.articleViewState.htmlWebViewRef.value!!.webView.findAllAsync(it) },
                onFindNext = { model.articleViewState.htmlWebViewRef.value!!.webView.findNext(true) },
                onClose = {
                  model.visibleFindBar = false
                  model.articleViewState.htmlWebViewRef.value!!.webView.clearMatches()
                  closeKeyboard()
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
private fun ComposedHeader(
  deepLinkMode: Boolean,
) {
  val model: ArticleScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  ArticleScreenHeader(
    title = model.displayPageName,
    visible = model.visibleHeader,
    deepLinkMode = deepLinkMode,
    onAction = {
      when(it) {
        REFRESH -> {
          scope.launch { model.articleViewState.reload(true) }
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
        SHOW_DRAWER -> {
          scope.launch { model.commonDrawerState.open() }
        }
      }
    }
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ComposedArticleView(
  arguments: ArticleRouteArguments,
) {
  val model: ArticleScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val isFocusMode by SettingsStore.common.getValue { focusMode }.collectAsState(initial = false)
  val isHideTopTemplates by SettingsStore.common.getValue { hideTopTemplates }.collectAsState(initial = false)

  val debouncedManualEffector = rememberDebouncedManualEffector<ReadingRecord>(1000) {
    scope.launch {
      SettingsStore.other.setValue {
        this.readingRecord = it
      }
    }
  }

//  val oldScrollValue = rememberSaveable { mutableListOf(0) }
//  FirstTimeSkippedLaunchedEffect(model.scrollState.value) {
//    if (model.articleLoadStatus != LoadStatus.SUCCESS) return@FirstTimeSkippedLaunchedEffect
//
//    val oldValue = oldScrollValue.first()
//    val value = model.scrollState.value
//
//    model.visibleHeader = value < 80 || value < oldValue
//    model.visibleCommentButton = value < oldValue
//    scope.launch {
//      debouncedManualEffector.trigger(ReadingRecord(
//        pageName = model.truePageName!!,
//        progress = value.toFloat() / model.articleViewRef.value!!.htmlWebViewRef!!.webView.contentHeight,
//        scrollY = value
//      ))
//    }
//
//    oldScrollValue[0] = value
//  }

  val handleOnScrollChanged: HtmlWebViewScrollChangeHandler = { _, top, _, oldTop ->
    if (isFocusMode) {
      model.visibleHeader = top == 0
      model.visibleCommentButton = top == 0
    } else {
      model.visibleHeader = top < 80 || top < oldTop
      model.visibleCommentButton = top < oldTop
    }

    scope.launch {
      debouncedManualEffector.trigger(ReadingRecord(
        pageName = model.truePageName!!,
        progress = top.toFloat() / model.articleViewState.htmlWebViewRef.value!!.webView.contentHeight,
        scrollY = top
      ))
    }
  }

//  LaunchedEffect(model.articleLoadStatus) {
//    model.swipeRefreshState.isRefreshing = model.articleLoadStatus == LoadStatus.LOADING
//  }

//  SwipeRefresh(
//    state = model.swipeRefreshState,
//    swipeEnabled = model.articleLoadStatus != LoadStatus.LOADING,
//    onRefresh = {
//      scope.launch { model.articleViewRef.value!!.reload(true) }
//    },
//    indicator = { state, refreshTriggerDistance ->
//      val headerHeight = Globals.statusBarHeight + Constants.topAppBarHeight
//
//      if (model.scrollState.value == 0) {
//        StyledSwipeRefreshIndicator(
//          modifier = Modifier
//            .padding(top = headerHeight.dp),
//          state = state,
//          refreshTriggerDistance = refreshTriggerDistance
//        )
//      }
//    }
//  ) {
//
//  }

  Center {
//    Box(
//      modifier = Modifier
//        .fillMaxSize()
//        .verticalScroll(model.scrollState),
//      contentAlignment = Alignment.Center
//    ) {
      val isShowCategories = !listOf("H萌娘:官方群组", "帮助:沙盒").contains(model.truePageName) && !isTalkPage(model.truePageName)
      ArticleView(
        state = model.articleViewState,
        pageKey = arguments.pageKey,
        revId = arguments.revId,
        editAllowed = model.editAllowed.allowed,
        visibleLoadStatusIndicator = false,
        contentTopPadding = (Constants.topAppBarHeight + Globals.statusBarHeight).dp,
        addCategories = isShowCategories,
        renderDelay = if (isHideTopTemplates) 500 else 0,
        onScrollChanged = handleOnScrollChanged,
        onArticleRendered = {
          scope.launch { model.handleOnArticleRendered() }
        },
        onPreGotoEdit = { model.handleOnPreGotoEdit() },
        onArticleMissed = { model.handleOnArticleMissed() },
        emitCatalogData = { model.catalogData = it },
        injectedScripts = listOf(
          BodyDoubleClickJs.scriptContent
        ),
        messageHandlers = mapOf(
          BodyDoubleClickJs.messageHandler
        )
      )
//    }

    AnimatedVisibility(
      visible = model.articleViewState.status == LoadStatus.LOADING,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      ArticleLoadingMask()
    }

    AnimatedVisibility(
      visible = model.articleViewState.status == LoadStatus.FAIL,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      ArticleErrorMask(
        onClick = {
          scope.launch { model.articleViewState.reload(true) }
        }
      )
    }
  }
}

class ReadingRecord(
  val pageName: String,
  val progress: Float,
  val scrollY: Int,
  val date: LocalDateTime = LocalDateTime.now()
)
