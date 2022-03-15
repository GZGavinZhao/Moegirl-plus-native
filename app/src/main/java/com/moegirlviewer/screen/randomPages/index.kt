package com.moegirlviewer.screen.randomPages

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.randomPages.component.RandomPageActionButton
import com.moegirlviewer.screen.randomPages.component.RandomPageItem
import com.moegirlviewer.screen.randomPages.component.RandomPageItemStatus
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.printDebugLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RandomPagesScreen() {
  val model: RandomPagesScreenModal = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  LaunchedEffect(model.pageList.size) {
    if (model.pageList.size < 10 && model.status != LoadStatus.LOADING) {
      model.loadPageList()
    }
  }

  LaunchedEffect(model.status) {
    if (model.status == LoadStatus.SUCCESS) {
      if (model.randomPageItemStates[0].status == RandomPageItemStatus.INITIAL) {
        model.randomPageItemStates[0].run {
          reset(model.popFirstFromPageList())
          rise()
        }
      }

      if (model.randomPageItemStates[1].status == RandomPageItemStatus.INITIAL) {
        delay(100)
        model.randomPageItemStates[1].run {
          reset(model.popFirstFromPageList())
          appear()
        }
      }
    }
  }

  Scaffold(
    topBar = {
      StyledTopAppBar(
        backgroundColor = themeColors.background,
        contentColor = themeColors.text.primary,
        statusBarDarkIcons = themeColors.isLight,
        title = {
          StyledText(
            text = stringResource(id = R.string.randomArticle),
          )
        },
        elevation = 3.dp
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        RandomPageItem(
          state = model.randomPageItemStates[0],
          onRemove = {
            scope.launch {
              model.randomPageItemStates[1].run { if (hasViewData) rise() }
              model.randomPageItemStates[0].run {
                reset(model.popFirstFromPageList())
                if (this.hasViewData) appear()
              }
            }
          },
        )

        RandomPageItem(
          state = model.randomPageItemStates[1],
          onRemove = {
            scope.launch {
              model.randomPageItemStates[0].run { if (hasViewData) rise() }
              model.randomPageItemStates[1].run {
                reset(model.popFirstFromPageList())
                if (this.hasViewData) appear()
              }
            }
          },
        )


        if (model.pageList.isEmpty()) {
          if (model.status == LoadStatus.LOADING) {
            StyledCircularProgressIndicator()
          } else {
            ReloadButton(
              onClick = { model.loadPageList() }
            )
          }
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp),
        contentAlignment = Alignment.TopCenter
      ) {
        RandomPageActionButton(
          onClick = {
            scope.launch { model.nextRandomPage() }
          }
        )
      }
    }
  }
}