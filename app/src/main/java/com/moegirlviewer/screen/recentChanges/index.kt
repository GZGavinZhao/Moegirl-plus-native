package com.moegirlviewer.screen.recentChanges

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
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
import com.moegirlviewer.component.TopAppBarIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.EmptyContent
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.recentChanges.component.RecentChangesItem
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.background2
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
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
          StyledText(
            text = stringResource(id = R.string.recentChanges),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = themeColors.onPrimary
          )
        },
        actions = {
          if (isLoggedIn) {
            TopAppBarIcon(
              image = if (isWatchListMode)
                ImageVector.vectorResource(id = R.drawable.eye) else
                Icons.Filled.FormatIndentDecrease,
              onClick = {
                scope.launch { model.toggleMode() }
              }
            )
          }

          TopAppBarIcon(
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
        for (itemData in model.changesList) {
          when(itemData) {
            is DateTitleItemAdapter -> {
              stickyHeader(key = itemData.key) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.background2)
                    .padding(top = 7.dp, bottom = 8.dp, start = 10.dp)
                ) {
                  StyledText(
                    text = itemData.dateTitle,
                  )
                }
              }
            }

            is DataItemAdapter -> {
              val data = itemData.data
              item(key = itemData.key) {
                RecentChangesItem(
                  type = data.type,
                  pageName = data.title,
                  comment = data.comment,
                  users = data.users,
                  newLength = data.newlen,
                  oldLength = data.oldlen,
                  revId = data.revid,
                  oldRevId = if (data.details.size > 1) data.details.last().revid else data.old_revid,
                  dateISO = data.timestamp,
                  editDetails = data.details,
                  hasMultiEditors = data.details.size > 1,
                  pageWatched = if (isWatchListMode && isLoggedIn) false else watchList.any { it.pageName == data.title }
                )
              }
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

        if (model.status == LoadStatus.EMPTY) {
          item(key = "empty") {
            EmptyContent()
          }
        }
      }
    }
  }
}