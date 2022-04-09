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
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.articleView.ArticleViewRef
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.isMoegirl
import com.moegirlviewer.util.printRequestErr
import kotlinx.coroutines.launch

@Composable
fun TopCard(
  state: TopCardState
) {
  val scope = rememberCoroutineScope()

  HomeCard(
    modifier = Modifier
      .padding(15.dp),
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
        previewMode = true,
        injectedStyles = state.injectedStyles,
        onStatusChanged = { state.status = it },
        fullHeight = true,
        renderDelay = 1000
      ))
    }
  }
}

class TopCardState : HomeScreenCardState() {
  var status by mutableStateOf(LoadStatus.LOADING)
  var isContentPageUpdated by mutableStateOf(false)
  val articleViewRef = Ref<ArticleViewRef>()
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
    articleViewRef.value?.reload?.invoke(true)
  }
}