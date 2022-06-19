package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moegirlviewer.Constants
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewState
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.PageKey
import com.moegirlviewer.util.PageNameKey
import kotlinx.coroutines.launch

@Composable
fun ArticleViewCard(
  state: ArticleViewCardState,
  pageKey: PageKey,
) {
  val scope = rememberCoroutineScope()

  HomeCard(
//    modifier = Modifier
//      .padding(15.dp),
    elevation = 0.dp,
    loadStatus = state.articleViewState.status,
    onReload = {
      scope.launch { state.reload() }
    },
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (state.articleViewState.status != LoadStatus.SUCCESS) Modifier.height(300.dp) else Modifier)
    ) {
      ArticleView(
        modifier = Modifier
          .heightIn(max = 1000.dp),
        state = state.articleViewState,
        pageKey = pageKey,
        visibleLoadStatusIndicator = false,
        previewMode = true,
        injectedStyles = state.injectedStyles,
        fullHeight = true,
        renderDelay = 1000
      )
    }
  }
}

class ArticleViewCardState : HomeScreenCardState() {
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
      height: 140px !important;
    }
  """.trimIndent())

  override suspend fun reload() {
    articleViewState.reload()
  }
}