package com.moegirlviewer.screen.contribution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.EmptyContent
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.contribution.component.ContributionItem
import com.moegirlviewer.ui.theme.background2
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@ExperimentalMaterialApi
@Composable
fun ContributionScreen(arguments: ContributionRouteArguments) {
  val model: ContributionScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  LaunchedEffect(true) {
    model.routeArguments = arguments
    if (model.status == LoadStatus.INITIAL) model.loadContributionList(true)
  }

  LaunchedEffect(model.status) {
    model.swipeRefreshState.isRefreshing = model.status == LoadStatus.INIT_LOADING
  }

  model.lazyListState.OnSwipeLoading() {
    scope.launch { model.loadContributionList() }
  }

  Scaffold(
    backgroundColor = themeColors.background2,
    topBar = {
      ComposedHeader(
        userName = arguments.userName
      )
    }
  ) {
    SwipeRefresh(
      state = model.swipeRefreshState,
      onRefresh = {
        scope.launch { model.loadContributionList(true) }
      },
      indicator = { state, trigger ->
        StyledSwipeRefreshIndicator(state, trigger)
      }
    ) {
      if (model.status != LoadStatus.EMPTY) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize(),
          state = model.lazyListState
        ) {
          itemsIndexed(
            items = model.contributionList,
            key = { _, item -> item.revid }
          ) { index, item ->
            ContributionItem(
              pageName = item.title,
              revId = item.revid,
              prevRevId = item.parentid,
              dateISO = item.timestamp,
              summary = item.comment,
              diffSize = item.sizediff,
            )
          }

          item {
            ScrollLoadListFooter(
              status = model.status,
              onReload = {
                scope.launch { model.loadContributionList() }
              }
            )
          }
        }
      } else {
        EmptyContent()
      }
    }
  }
}

@Composable
private fun ComposedHeader(
  userName: String
) {
  val model: ContributionScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  Column() {
    StyledTopAppBar(
      title = {
        Text(
          text = stringResource(id = R.string.userContribution) + "：" + userName,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    )

    RippleColorScope(color = themeColors.onPrimary) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .background(themeColors.primary),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Center(
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable {
              scope.launch { model.showDatePickerDialog() }
            }
        ) {
          Text(
            text = model.startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            color = themeColors.onPrimary,
            fontSize = 16.sp
          )
        }

        Text(
          modifier = Modifier
            .padding(horizontal = 10.dp),
          text = "-",
          color = themeColors.onPrimary
        )

        Center(
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clickable {
              scope.launch { model.showDatePickerDialog(false) }
            }
        ) {
          Text(
            text = model.endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            color = themeColors.onPrimary,
            fontSize = 16.sp
          )
        }
      }
    }
  }
}