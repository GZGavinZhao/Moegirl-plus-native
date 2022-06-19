package com.moegirlviewer.screen.pageRevisions

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.component.EmptyContent
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.component.styled.TopAppBarTitle
import com.moegirlviewer.screen.pageRevisions.component.RevisionItem
import com.moegirlviewer.theme.background2
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PageVersionHistoryScreen(arguments: PageRevisionsRouteArguments) {
  val model: PageRevisionsScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  LaunchedEffect(true) {
    model.routeArguments = arguments
    if (model.status == LoadStatus.INITIAL) model.loadRevisionList(true)
  }

  LaunchedEffect(model.status) {
    model.swipeRefreshState.isRefreshing = model.status == LoadStatus.INIT_LOADING
  }

  model.lazyListState.OnSwipeLoading() {
    scope.launch { model.loadRevisionList() }
  }

  Scaffold(
    backgroundColor = themeColors.background2,
    topBar = {
      StyledTopAppBar(
        title = {
          TopAppBarTitle(
            text = stringResource(id = R.string.versionHistory) + "：" + arguments.pageName,
            twoRows = true,
          )
        }
      )
    }
  ) {
    SwipeRefresh(
      state = model.swipeRefreshState,
      onRefresh = {
        scope.launch { model.loadRevisionList(true) }
      },
      indicator = { state, trigger ->
        StyledSwipeRefreshIndicator(state, trigger)
      }
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize(),
        state = model.lazyListState
      ) {
        itemsIndexed(
          items = model.revisionList,
          key = { _, item -> item.revid }
        ) { index, item ->
          // 接口没有提供更改的大小，只能自己算，这样当前加载的最后一条没法计算，就传null，组件内部进行判断
          val diffSize = when {
            item.parentid == 0 -> item.size
            index + 1 < model.revisionList.size -> item.size - model.revisionList[index + 1].size
            else -> null
          }

          RevisionItem(
            pageName = arguments.pageName,
            revId = item.revid,
            prevRevId = item.parentid,
            userName = item.user,
            dateISO = item.timestamp,
            summary = item.comment,
            diffSize = diffSize,
            visibleCurrentCompareButton = index != 0,   // 第一条不显示“当前”按钮
            visiblePrevCompareButton = item.parentid != 0   // 最后一条不显示“之前”按钮
          )
        }

        item {
          if (model.status != LoadStatus.EMPTY) {
            ScrollLoadListFooter(
              status = model.status,
              onReload = {
                scope.launch { model.loadRevisionList() }
              }
            )
          } else {
            EmptyContent()
          }
        }
      }
    }
  }
}