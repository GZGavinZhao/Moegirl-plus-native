package com.moegirlviewer.screen.compare

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LowPriority
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.compare.component.CompareScreenDiffContent
import com.moegirlviewer.screen.compare.util.showUndoDialog
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@Composable
fun CompareScreen(
  arguments: CompareRouteArguments
) {
  val model: CompareScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  SideEffect {
    model.routeArguments = arguments
  }

  LaunchedEffect(true) {
    if (model.status == LoadStatus.INITIAL) {
      model.loadCompareData()
    }
  }

  LaunchedEffect(model.selectedTabIndex) {
    model.pagerState.animateScrollToPage(model.selectedTabIndex)
  }

  LaunchedEffect(model.pagerState.currentPage) {
    model.selectedTabIndex = model.pagerState.currentPage
  }

  Scaffold(
    topBar = {
      ComposedHeader(
        pageName = if (arguments is ComparePageRouteArguments)
          arguments.pageName else ""
      )
    }
  ) {
    when(model.status) {
      LoadStatus.SUCCESS -> {
        HorizontalPager(
//          count = 2,
          state = model.pagerState,
        ) { currentIndex ->
          if (currentIndex == 0) {
            CompareScreenDiffContent(
              userName = model.compareData!!.fromuser,
              comment = if (model.compareData!!.fromcomment != "") model.compareData!!.fromcomment else null,
              diffLines = model.leftLines,
              isCompareText = model.isCompareTextMode
            )
          } else {
            CompareScreenDiffContent(
              userName = model.compareData!!.touser,
              comment = if (model.compareData!!.tocomment != "") model.compareData!!.tocomment else null,
              diffLines = model.rightLines,
              isCompareText = model.isCompareTextMode
            )
          }
        }
      }

      LoadStatus.LOADING -> {
        Center {
          StyledCircularProgressIndicator()
        }
      }

      LoadStatus.FAIL -> {
        Center {
          ReloadButton(
            onClick = {
              scope.launch { model.loadCompareData() }
            }
          )
        }
      }
      else -> {}
    }
  }
}

@ExperimentalPagerApi
@Composable
private fun ComposedHeader(
  pageName: String
) {
  val model: CompareScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)
  val titles = remember { listOf(
    Globals.context.getString(R.string.before),
    Globals.context.getString(R.string.after)
  ) }

  Surface(
    elevation = 5.dp
  ) {
    Column() {
      StyledTopAppBar(
        navigationIcon = {
          BackButton()
        },
        title = {
          StyledText(
            text = if (model.isCompareTextMode)
              stringResource(id = R.string.diffCompare) else
              "${stringResource(id = R.string.diff)}：$pageName",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = themeColors.onPrimary
          )
        },
        actions = {
          if (isLoggedIn && !model.isCompareTextMode) {
            AppHeaderIcon(
              image = Icons.Filled.LowPriority,
              onClick = {
                showUndoDialog(model)
              }
            )
          }
        }
      )

      TabRow(
        selectedTabIndex = model.selectedTabIndex
      ) {
        titles.forEachIndexed { index, title ->
          Tab(
            text = {
              StyledText(
                text = title,
                color = Color.Unspecified
              )
            },
            selected = model.selectedTabIndex == index,
            onClick = { model.selectedTabIndex = index }
          )
        }
      }
    }
  }
}