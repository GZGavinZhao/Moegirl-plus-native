package com.moegirlviewer.screen.browsingHistory

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.TopAppBarIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.EmptyContent
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.screen.browsingHistory.component.BrowsingHistoryScreenItem
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.vibrate

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun BrowsingHistoryScreen() {
  val model: BrowsingHistoryScreenModel = hiltViewModel()
  val configuration = LocalConfiguration.current
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  LaunchedEffect(true) {
    if (model.status == LoadStatus.INITIAL) model.refreshList()
  }

  Scaffold(
    backgroundColor = themeColors.background2,
    topBar = {
      ComposedHeader()
    }
  ) {
    when(model.status) {
      LoadStatus.ALL_LOADED -> {
        LazyColumn {
          fun listBlock(
            titleResourceId: Int,
            list: List<BrowsingRecord>
          ) {
            if (list.isNotEmpty()) {
              stickyHeader {
                Title(stringResource(titleResourceId))
              }

              itemsIndexed(
                items = list,
                key = { _, item -> item.pageName }
              ) { _, item ->
                BrowsingHistoryScreenItem(
                  record = item,
                  onClick = { gotoArticlePage(item.pageName) },
                  onLongClick = {
                    vibrate()
                    model.deleteRecord(item)
                  }
                )
              }
            }
          }

          listBlock(R.string.today, model.lists.todayList)
          listBlock(R.string.yesterday, model.lists.yesterdayList)
          listBlock(R.string.earlier, model.lists.earlierList)
        }
      }

      LoadStatus.EMPTY -> {
        EmptyContent(
          message = stringResource(id = R.string.noRecord),
        )
//        Box(
//          modifier = Modifier.fillMaxSize(),
//          contentAlignment = Alignment.Center
//        ) {
//          StyledText(
//            text = stringResource(id = R.string.noRecord),
//            fontSize = 18.sp,
//            color = themeColors.text.tertiary
//          )
//        }
      }

      else -> {
        Box(
          modifier = Modifier
            .fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          StyledCircularProgressIndicator()
        }
      }
    }
  }
}

@Composable
private fun ComposedHeader() {
  val themeColors = MaterialTheme.colors
  val model: BrowsingHistoryScreenModel = hiltViewModel()

  StyledTopAppBar(
    navigationIcon = {
      BackButton()
    },
    title = {
      StyledText(
        text = stringResource(id = R.string.browseHistory),
        color = themeColors.onPrimary
      )
    },
    actions = {
      TopAppBarIcon(
        image = Icons.Filled.Search,
        onClick = {
          Globals.navController.navigate("browsingHistorySearch")
        }
      )
      TopAppBarIcon(
        image = Icons.Filled.Delete,
        onClick = {
          model.deleteAllRecords()
        }
      )
    }
  )
}

@Composable
private fun Title(
  text: String
) {
  val themeColors = MaterialTheme.colors

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(themeColors.background2)
      .padding(bottom = 10.dp, top = 10.dp)
  ) {
    StyledText(
      modifier = Modifier
        .padding(start = 10.dp),
      text = text,
      color = themeColors.text.primary,
      fontSize = 16.sp
    )

//    Spacer(modifier = Modifier
//      .padding(top = 3.dp, end = 10.dp)
//      .height(2.dp)
//      .fillMaxWidth()
//      .background(themeColors.primaryVariant)
//    )
  }
}