package com.moegirlviewer.screen.search.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.api.search.SearchApi
import com.moegirlviewer.component.styled.StyleLinearProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.search.SearchScreenModel
import com.moegirlviewer.store.SearchRecord
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.SearchScreenHintList() {
  val model: SearchScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  var list by rememberSaveable { mutableStateOf(listOf<String>()) }
  var status by rememberSaveable { mutableStateOf(LoadStatus.INITIAL) }

  suspend fun loadHintList() = scope.launch {
    if (model.keywordInputValue.trim() == "") { return@launch }
    status = LoadStatus.LOADING
    try {
      status = LoadStatus.LOADING
      list = SearchApi.getHint(model.keywordInputValue).query.prefixsearch.map { it.title }
      status = if (list.isEmpty()) LoadStatus.EMPTY else LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      status = LoadStatus.FAIL
      toast(Globals.context.getString(R.string.netErr))
      printRequestErr(e, "加载搜索提示失败")
    }
  }

  LaunchedEffect(model.keywordInputValue) {
    loadHintList()
  }

  Column(
    modifier = Modifier
      .weight(1f)
      .verticalScroll(rememberScrollState())
  ) {
    if (status == LoadStatus.LOADING) {
      StyleLinearProgressIndicator()
    }

    for (item in list) {
      Item(
        text = item,
        onClick = { model.searchByRecord(SearchRecord(item, true)) }
      )
    }

    if (status == LoadStatus.FAIL) {
      Item(text = stringResource(id = R.string.netErr))
    }
  }
}

@Composable
private fun Item(
  text: String,
  onClick: (() -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val density = LocalDensity.current.density

  Surface(
    contentColor = themeColors.text.primary
  ) {
    Box(
      modifier = Modifier
        .height(42.dp)
        .fillMaxWidth()
        .sideBorder(BorderSide.BOTTOM, (1 / density).dp, themeColors.text.tertiary)
        .clickable { onClick?.invoke() }
        .padding(horizontal = 10.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      StyledText(
        text = text,
        color = themeColors.text.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}