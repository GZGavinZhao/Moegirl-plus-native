package com.moegirlviewer.screen.searchResult

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.component.styled.TopAppBarTitle
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.searchResult.component.SearchResultItem
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.PageNameKey
import com.moegirlviewer.util.navigate
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SearchResultScreen(
  arguments: SearchResultRouteArguments
) {
  val model: SearchResultScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
//  val bgColor = if (themeColors.isLight) Color.White else themeColors.primary

  LaunchedEffect(true) {
    model.routeArguments = arguments
    if (model.status == LoadStatus.INITIAL) model.loadList()
  }

  model.listState.OnSwipeLoading() {
    scope.launch { model.loadList() }
  }

  Scaffold(
    topBar = {
      StyledTopAppBar(
        backgroundColor = themeColors.primary,
        title = {
          TopAppBarTitle(
            text = stringResource(id = R.string.search) + "：" + arguments.keyword,
            twoRows = true,
          )
        }
      )
    }
  ) {
    RippleColorScope(color = themeColors.primaryVariant) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .background(themeColors.background2),
        state = model.listState
      ) {
        item {
          AnimatedVisibility(
            visible = model.resultList.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            StyledText(
              modifier = Modifier
                .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),
              text = stringResource(id = R.string.searchResultTotal, model.resultTotal),
              color = themeColors.text.secondary
            )
          }
        }

        itemsIndexed(
          items = model.resultList,
          key = { _, item -> item.pageid }
        ) { _, item ->
          SearchResultItem(
            data = item,
            keyword = arguments.keyword,
            onClick = {
              Globals.navController.navigate(ArticleRouteArguments(
                pageKey = PageNameKey(item.title)
              ))
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