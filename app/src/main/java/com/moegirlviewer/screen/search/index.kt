package com.moegirlviewer.screen.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.imePadding
import com.moegirlviewer.screen.search.component.SearchScreenHeader
import com.moegirlviewer.screen.search.component.SearchScreenHintList
import com.moegirlviewer.screen.search.component.SearchScreenRecentSearch
import com.moegirlviewer.util.imeBottomPadding

@ExperimentalFoundationApi
@Composable
fun SearchScreen() {
  val model: SearchScreenModel = hiltViewModel()

  Scaffold(
    modifier = Modifier
      .imeBottomPadding(),
    topBar = {
      SearchScreenHeader()
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
    ) {
      if (model.keywordInputValue.trim() == "") {
        SearchScreenRecentSearch()
      } else {
        SearchScreenHintList()
      }
    }
  }
}