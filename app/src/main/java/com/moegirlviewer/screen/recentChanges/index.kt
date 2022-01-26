package com.moegirlviewer.screen.recentChanges

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.recentChanges.component.RecentChangesItem
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.ui.theme.background2
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RecentChangesScreen() {
  val model: RecentChangesScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)
  val isWatchListMode by SettingsStore.recentChanges.getValue { this.isWatchListMode }.collectAsState(
    initial = false
  )
  val watchList by Globals.room.watchingPage().getAll().collectAsState(initial = emptyList())

  LaunchedEffect(true) {
    if (model.status == LoadStatus.INITIAL) {
      delay(400)  // 延迟加载防止进入动画前就加载完成，一起渲染导致卡顿
      model.loadList()
    }
  }

  LaunchedEffect(model.status) {
    model.swipeRefreshState.isRefreshing = model.status == LoadStatus.LOADING
  }

  Scaffold(
    backgroundColor = themeColors.background2,
    topBar = {
      StyledTopAppBar(
        navigationIcon = {
          BackButton()
        },
        title = {
          Text(
            text = stringResource(id = R.string.recentChanges),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        actions = {
          if (isLoggedIn) {
            AppHeaderIcon(
              image = if (isWatchListMode)
                ImageVector.vectorResource(id = R.drawable.eye) else
                Icons.Filled.FormatIndentDecrease,
              onClick = {
                scope.launch { model.toggleMode() }
              }
            )
          }

          AppHeaderIcon(
            image = Icons.Filled.Tune,
            onClick = {
              scope.launch { model.showSettingsDialog() }
            }
          )
        },
      )
    }
  ) {
    SwipeRefresh(
      state = model.swipeRefreshState,
      onRefresh = {
        scope.launch { model.loadList() }
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
          items = model.changesList,
          key = { _, item -> item.key }
        ) { _, adapter ->
          when(adapter) {
            is DateTitleItemAdapter -> {
              Text(
                modifier = Modifier
                  .padding(top = 7.dp, bottom = 8.dp, start = 10.dp),
                text = adapter.dateTitle,
              )
            }

            is DataItemAdapter -> {
              val item = adapter.data
              RecentChangesItem(
                type = item.type,
                pageName = item.title,
                comment = item.comment,
                users = item.users,
                newLength = item.newlen,
                oldLength = item.oldlen,
                revId = item.revid,
                oldRevId = item.old_revid,
                dateISO = item.timestamp,
                editDetails = item.details,
                pageWatched = if (isWatchListMode && isLoggedIn) false else watchList.any { it.pageName == item.title }
              )
            }
          }
        }

        if (model.status == LoadStatus.SUCCESS || model.status == LoadStatus.FAIL) {
          item(key = "footer") {
            ScrollLoadListFooter(
              status = model.status,
              errorText = stringResource(id = R.string.loadErrToSwipeRetry)
            )
          }
        }
      }
    }
  }
}