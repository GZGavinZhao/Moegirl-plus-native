package com.moegirlviewer.screen.search.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.sideBorder
import com.moegirlviewer.screen.search.SearchScreenModel
import com.moegirlviewer.store.SearchRecordsStore
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.vibrate
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun ColumnScope.SearchScreenRecentSearch() {
  val searchRecords by SearchRecordsStore.searchRecords.collectAsState(initial = emptyList())

  if (searchRecords.isEmpty()) {
    ComposedEmpty()
  } else {
    Column(
      modifier = Modifier
        .weight(1f)
    ) {
      ComposedHeader()
      ComposedRecordList()
    }
  }
}

@Composable
private fun ColumnScope.ComposedEmpty() {
  val themeColors = MaterialTheme.colors

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text= stringResource(id = R.string.noSearchRecord),
      fontSize = 18.sp,
      color = themeColors.text.tertiary,
    )
  }
}

@Composable
private fun ComposedHeader() {
  val model: SearchScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(40.dp)
      .padding(start = 12.dp, end = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = stringResource(id = R.string.recentSearch),
      color = themeColors.text.secondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

    IconButton(
      onClick = { model.showClearSearchRecordModal() }
    ) {
      Icon(
        modifier = Modifier
          .width(18.dp)
          .height(18.dp),
        imageVector = Icons.Filled.Delete,
        contentDescription = null,
        tint = themeColors.text.secondary,
      )
    }
  }
}

@ExperimentalFoundationApi
@Composable
private fun ColumnScope.ComposedRecordList() {
  val model: SearchScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val list by model.searchRecords.collectAsState(initial = emptyList())
  val scrollState = rememberScrollState(0)

  Column(
    modifier = Modifier
      .weight(1f)
      .fillMaxWidth()
      .verticalScroll(scrollState)
  ) {
    for (item in list) {
      RecordItem(
        keyword = item.keyword,
        themeColors = themeColors,
        onClick = { model.searchByRecord(item) },
        onRemove = {
          vibrate()
          scope.launch { model.showRemoveSearchRecordModal(item.keyword) }
        },
      )
    }
  }
}

@ExperimentalFoundationApi
@Composable
private fun RecordItem(
  keyword: String,
  themeColors: Colors,
  onClick: () -> Unit,
  onRemove: () -> Unit,
) {
  val density = LocalDensity.current.density

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(42.dp)
      .sideBorder(BorderSide.BOTTOM, (1 / density).dp, themeColors.text.tertiary)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onRemove
      )
      .padding(start = 12.dp)
    ,
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = keyword,
      color = themeColors.text.secondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}