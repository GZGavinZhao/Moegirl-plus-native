package com.moegirlviewer.screen.notification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
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
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.notification.component.NotificationScreenItem
import com.moegirlviewer.ui.theme.background2
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.gotoArticlePage
import kotlinx.coroutines.launch


@Composable
fun NotificationScreen() {
  val model: NotificationScreenModel = hiltViewModel()
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
      ComposedHeader()
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
          items = model.notificationList,
          key = { _, item -> item.id }
        ) { _, item ->
          NotificationScreenItem(
            notification = item,
            onClick = {
              if (item.title != null) {
                gotoArticlePage(item.title.full)
              }
            }
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

@Composable
private fun ComposedHeader() {
  val model: NotificationScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  StyledTopAppBar(
    title = {
      Text(stringResource(id = R.string.notification))
    },
    actions = {
      AppHeaderIcon(
        image = Icons.Filled.DoneAll,
        onClick = {
          scope.launch { model.markAllAsRead() }
        }
      )
    }
  )
}