package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.dp
import com.moegirlviewer.Constants
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.articleView.ArticleViewRef
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.isMoegirl
import kotlinx.coroutines.launch

@Composable
fun TopCard(
  state: TopCardState
) {
  val scope = rememberCoroutineScope()

  HomeCard(
    loadStatus = state.status,
    onReload = {
      scope.launch { state.reload() }
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (state.status != LoadStatus.SUCCESS) Modifier.height(300.dp) else Modifier)
    ) {
      ArticleView(props = ArticleViewProps(
        ref = state.articleViewRef,
        pageName = Constants.topCardContentPageName,
        visibleLoadStatusIndicator = false,
        injectedStyles = state.injectedStyles,
        onStatusChanged = { state.status = it },
        fullHeight = true,
        renderDelay = 1000
      ))
    }
  }
}

class TopCardState : HomeScreenCardState() {
  var status by mutableStateOf(LoadStatus.INITIAL)
  val articleViewRef = Ref<ArticleViewRef>()
  val injectedStyles = listOf("""
    body { 
      padding-bottom: 0;
      margin-top: 1em;
    }
  """.trimIndent())

  override suspend fun reload() {
    articleViewRef.value?.reload?.invoke(true)
  }
}