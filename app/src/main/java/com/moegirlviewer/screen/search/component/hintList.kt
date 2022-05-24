package com.moegirlviewer.screen.search.component

import android.inputmethodservice.Keyboard
import android.os.Parcelable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.api.search.SearchApi
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyleLinearProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.search.SearchScreenModel
import com.moegirlviewer.screen.searchResult.SearchResultRouteArguments
import com.moegirlviewer.store.SearchRecord
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Composable
fun ColumnScope.SearchScreenHintList() {
  val model: SearchScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val lazyState = rememberLazyListState()
  var list by rememberSaveable { mutableStateOf(listOf<SearchHintItem>()) }
  var status by rememberSaveable { mutableStateOf(LoadStatus.INITIAL) }
  var lastReloadSearchKeyword by rememberSaveable { mutableStateOf("") }

  suspend fun loadNext(reload: Boolean = false) = scope.launch {
    if (model.keywordInputValue.trim() == "") return@launch
    if (LoadStatus.isCannotLoad(status) && !reload) return@launch
    if (lastReloadSearchKeyword == model.keywordInputValue && !reload) return@launch

    lastReloadSearchKeyword = model.keywordInputValue
    status = if (reload) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    if (reload) list = emptyList()
    try {
      val res = SearchApi.getHint(
        keyword = model.keywordInputValue,
        offset = list.size,
      )
      val nextList = res.query.prefixsearch.map { SearchHintItem(
        title = it.title,
        subtext = res.query.pages[it.pageid]?.extract?.let { if (it == "") null else it },
        imageUrl = res.query.pages[it.pageid]?.thumbnail?.source,
      ) }

      status = when {
        list.isEmpty() && nextList.isEmpty() -> LoadStatus.EMPTY
        list.isNotEmpty() && nextList.isEmpty() -> LoadStatus.ALL_LOADED
        else -> LoadStatus.SUCCESS
      }

      list += nextList
    } catch (e: MoeRequestException) {
      status = LoadStatus.FAIL
      toast(Globals.context.getString(R.string.netErr))
      printRequestErr(e, "加载搜索提示失败")
    }
  }

  LaunchedEffect(model.keywordInputValue) {
    loadNext(true)
  }

  lazyState.OnSwipeLoading {
    scope.launch { loadNext() }
  }

  LazyColumn(
    modifier = Modifier
      .weight(1f),
    state = lazyState
  ) {
    item {
      AnimatedVisibility(
        visible = status == LoadStatus.INIT_LOADING,
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

    if (model.keywordInputValue != "") {
      item {
        SearchInPagesContentHint()
      }
    }

    itemsIndexed(
      items = list,
      key = { _, item -> item.title }
    ) { _, item ->
      Item(
        text = item.title,
        imageUrl = item.imageUrl,
        subtext = item.subtext,
        onClick = { model.searchByRecord(SearchRecord(item.title, true)) }
      )
    }

    item {
      ScrollLoadListFooter(
        status = status,
        onReload = {
          scope.launch { loadNext() }
        }
      )
    }
  }
}

@Composable
private fun Item(
  text: String,
  subtext: String? = null,
  imageUrl: String? = null,
  onClick: (() -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val density = LocalDensity.current.density

  RippleColorScope(color = themeColors.primaryVariant) {
    Surface(
      contentColor = themeColors.text.primary
    ) {
      Row(
        modifier = Modifier
          .height(80.dp)
          .fillMaxWidth()
          .sideBorder(BorderSide.BOTTOM, (1 / density).dp, themeColors.text.tertiary)
          .clickable { onClick?.invoke() }
          .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
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
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background2),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                modifier = Modifier
                  .fillMaxSize(0.8f),
                imageVector = Icons.Filled.TextSnippet,
                contentDescription = null,
                tint = themeColors.text.tertiary
              )
            }
          }
        }

        Column(
          modifier = Modifier
            .padding(start = 10.dp)
            .fillMaxHeight(),
          verticalArrangement = Arrangement.Center
        ) {
          StyledText(
            text = text,
            color = themeColors.text.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          if (subtext != null) {
            StyledText(
              modifier = Modifier
                .padding(top = 3.dp),
              text = subtext,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              color = themeColors.text.secondary.copy(0.8f),
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SearchInPagesContentHint() {
  val themeColors = MaterialTheme.colors
  val model: SearchScreenModel = hiltViewModel()

  RippleColorScope(color = themeColors.primaryVariant) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clickable {
          Globals.navController.navigate(SearchResultRouteArguments(model.keywordInputValue))
        }
        .padding(start = 10.dp, end = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        modifier = Modifier
          .size(30.dp),
        imageVector = Icons.Filled.FindInPage,
        contentDescription = null,
        tint = themeColors.primaryVariant
      )

      StyledText(
        modifier = Modifier
          .padding(start = 10.dp),
        text = stringResource(id = R.string.searchInPagesContent),
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline,
        color = themeColors.primaryVariant
      )
    }
  }
}

@Parcelize
class SearchHintItem(
  val title: String,
  val subtext: String?,
  val imageUrl: String?,
) : Parcelable