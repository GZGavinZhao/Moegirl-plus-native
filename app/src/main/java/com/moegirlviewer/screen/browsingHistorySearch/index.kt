package com.moegirlviewer.screen.browsingHistorySearch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.imePadding
import com.moegirlviewer.R
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.browsingHistory.component.BrowsingHistoryScreenItem
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.autoFocus
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.imeBottomPadding
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun BrowsingHistorySearchScreen() {
  val model: BrowsingHistorySearchScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  Scaffold(
    modifier = Modifier
      .imeBottomPadding(),
    topBar = {
      ComposedHeader(
        searchInputVal = model.searchInputVal,
        onSearchInputValChange = {
          model.searchInputVal = it
          scope.launch { model.search() }
        }
      )
    }
  ) {
    if (model.searchingResult.isNotEmpty()) {
      LazyColumn {
        itemsIndexed(
          items = model.searchingResult,
          key = { _, item -> item.pageName }
        ) { _, item ->
          BrowsingHistoryScreenItem(
            record = item,
            onClick = { gotoArticlePage(item.pageName) }
          )
        }
      }
    } else {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stringResource(id = R.string.noSearchingResult),
          color = themeColors.text.tertiary,
          fontSize = 18.sp
        )
      }
    }
  }
}

@Composable
private fun ComposedHeader(
  searchInputVal: String,
  onSearchInputValChange: (text: String) -> Unit,
) {
  val themeColors = MaterialTheme.colors

  StyledTopAppBar(
    title = {
      PlainTextField(
        modifier = Modifier
          .fillMaxWidth()
          .autoFocus(),
        value = searchInputVal,
        singleLine = true,
        onValueChange = onSearchInputValChange,
        placeholder = stringResource(id = R.string.searchInBrowsingHistory),
        cursorBrush = SolidColor(themeColors.onPrimary),
        textStyle = TextStyle(
          color = themeColors.onPrimary,
          fontSize = 18.sp
        ),
        placeholderStyle = TextStyle(
          color = themeColors.onPrimary.copy(alpha = 0.5f),
          fontSize = 18.sp
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Done
        ),
        maxLines = 1,
      )
    },
    actions = {
      if (searchInputVal != "") {
        IconButton(
          onClick = { onSearchInputValChange("") },
        ) {
          Icon(
            modifier = Modifier
              .width(20.dp)
              .height(20.dp),
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            tint = themeColors.onPrimary
          )
        }
      }
    }
  )
}