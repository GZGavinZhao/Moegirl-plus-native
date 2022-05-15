package com.moegirlviewer.screen.search.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.api.search.SearchApi
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyleLinearProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.search.SearchScreenModel
import com.moegirlviewer.store.SearchRecord
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.SearchScreenHintList() {
  val model: SearchScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val lazyState = rememberLazyListState()
  var list by rememberSaveable { mutableStateOf(listOf<SearchHintItem>()) }
  var status by rememberSaveable { mutableStateOf(LoadStatus.INITIAL) }
  var prevSearchKeyword by rememberSaveable { mutableStateOf("") }

  suspend fun load() = scope.launch {
    if (model.keywordInputValue.trim() == "") { return@launch }
    status = LoadStatus.LOADING
    prevSearchKeyword = model.keywordInputValue
    try {
      status = LoadStatus.LOADING
      val res = SearchApi.getHint(
        keyword = model.keywordInputValue,
        limit = 100
      )
      val nextList = res.query.prefixsearch.map { SearchHintItem(
        title = it.title,
        imageUrl = res.query.pages[it.pageid]?.thumbnail?.source
      ) }

      status = if (nextList.isEmpty()) LoadStatus.EMPTY else LoadStatus.SUCCESS
//      status = when {
//        list.isEmpty() && nextList.isEmpty() -> LoadStatus.EMPTY
//        list.isNotEmpty() && nextList.isEmpty() -> LoadStatus.ALL_LOADED
//        else -> LoadStatus.SUCCESS
//      }

      list = nextList
    } catch (e: MoeRequestException) {
      status = LoadStatus.FAIL
      toast(Globals.context.getString(R.string.netErr))
      printRequestErr(e, "加载搜索提示失败")
    }
  }

  LaunchedEffect(model.keywordInputValue) {
    load()
  }

  LazyColumn(
    modifier = Modifier
      .weight(1f),
    state = lazyState
  ) {
    item {
      AnimatedVisibility(
        visible = status == LoadStatus.LOADING,
        enter = expandVertically(
          expandFrom = Alignment.Top,
          animationSpec = tween(
            durationMillis = 200
          )
        ),
        exit = shrinkVertically(
          shrinkTowards = Alignment.Top,
          animationSpec = tween(
            durationMillis = 200
          )
        )
      ) {
        StyleLinearProgressIndicator()
      }
    }

    itemsIndexed(
      items = list,
      key = { _, item -> item.title }
    ) { _, item ->
      Item(
        text = item.title,
        imageUrl = item.imageUrl,
        onClick = { model.searchByRecord(SearchRecord(item.title, true)) }
      )
    }
  }
}

@Composable
private fun Item(
  text: String,
  imageUrl: String? = null,
  visibleImageUrl: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val density = LocalDensity.current.density

  Surface(
    contentColor = themeColors.text.primary
  ) {
    Row(
      modifier = Modifier
        .height(60.dp)
        .fillMaxWidth()
        .sideBorder(BorderSide.BOTTOM, (1 / density).dp, themeColors.text.tertiary)
        .clickable { onClick?.invoke() }
        .padding(end = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (visibleImageUrl) {
        Box(
          modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
        ) {
          if (imageUrl != null) {
            AsyncImage(
              modifier = Modifier
                .fillMaxSize(),
              model = rememberImageRequest(data = imageUrl),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              alignment = Alignment.TopCenter
            )
          } else {
            AsyncImage(
              modifier = Modifier
                .fillMaxSize(),
              model = rememberImageRequest(data = R.drawable.placeholder),
              contentDescription = null,
              contentScale = ContentScale.Fit,
            )
          }
        }
      }

      StyledText(
        modifier = Modifier
          .padding(start = 10.dp),
        text = text,
        color = themeColors.text.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

class SearchHintItem(
  val title: String,
  val imageUrl: String?
)