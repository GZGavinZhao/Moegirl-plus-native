package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moegirlviewer.Constants
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewState
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.PageNameKey
import kotlinx.coroutines.launch

@Composable
fun TopCard(
  state: TopCardState
) {
  val scope = rememberCoroutineScope()

  HomeCard(
    modifier = Modifier
      .padding(15.dp),
    loadStatus = state.articleViewState.status,
    onReload = {
      scope.launch { state.reload() }
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (state.articleViewState.status != LoadStatus.SUCCESS) Modifier.height(300.dp) else Modifier)
    ) {
      ArticleView(
        state = state.articleViewState,
        pageKey = PageNameKey(Constants.topCardContentPageName),
        visibleLoadStatusIndicator = false,
        previewMode = true,
        injectedStyles = state.injectedStyles,
        fullHeight = true,
        renderDelay = 1000
      )
    }
  }
}

class TopCardState : HomeScreenCardState() {
  var isContentPageUpdated by mutableStateOf(false)
  val articleViewState = ArticleViewState()
  val injectedStyles = listOf("""
    body { 
      padding-bottom: 0;
      margin-top: 1em;
    }
    
    ul.gallery li:not(foo) {
      width: 90px;
    }
    
    ul.gallery li img:not(foo) {
      width: 90px !important;
      height: 130px;
    }
  """.trimIndent())

//  suspend fun purgePage() {
//    isContentPageUpdated = false
//    try {
//      PageApi.purgePage(Constants.topCardContentPageName)
//      isContentPageUpdated = true
//    } catch (e: MoeRequestException) {
//      printRequestErr(e, "刷新topCard内容页面失败")
//    }
//  }

  override suspend fun reload() {
//    purgePage()
    articleViewState.reload()
  }
}