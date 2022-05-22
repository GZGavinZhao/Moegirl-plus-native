package com.moegirlviewer.screen.newPages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.newPages.component.NewPageItem
import com.moegirlviewer.screen.notification.component.NotificationScreenItem
import com.moegirlviewer.theme.background2
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.gotoArticlePage
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NewPagesScreen() {
  val model: NewPagesScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  LaunchedEffect(true) {
    if (model.status == LoadStatus.INITIAL) model.loadList(true)
  }

  LaunchedEffect(model.status) {
    model.swipeRefreshState.isRefreshing = model.status == LoadStatus.INIT_LOADING
  }

  model.lazyListState.OnSwipeLoading {
    scope.launch { model.loadList() }
  }

  Scaffold(
    backgroundColor = themeColors.background2,
    topBar = {
      StyledTopAppBar(
        title = {
          StyledText(
            text = stringResource(id = R.string.newArticles),
            color = themeColors.onPrimary
          )
        },
      )
    }
  ) {
    SwipeRefresh(
      state = model.swipeRefreshState,
      onRefresh = {
        scope.launch { model.loadList(true) }
      },
      indicator = { state, trigger ->
        StyledSwipeRefreshIndicator(state, trigger)
      }
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize(),
        state = model.lazyListState,
      ) {
        itemsIndexed(
          items = model.newPageList,
          key = { _, item -> item.pageid }
        ) { _, item ->
          NewPageItem(
            text = item.title,
            subtext = item.extract,
            imageUrl = item.thumbnail?.source,
            onClick = { gotoArticlePage(item.title) }
          )
        }

        item {
          ScrollLoadListFooter(
            status = model.status,
            onReload = {
              scope.launch { model.loadList() }
            }
          )
        }
      }
    }
  }
}