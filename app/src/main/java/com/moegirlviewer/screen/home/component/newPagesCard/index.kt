package com.moegirlviewer.screen.home.component.newPagesCard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.moegirlviewer.R
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.page.bean.PageProfileBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.screen.home.component.HomeCardContainer
import com.moegirlviewer.screen.home.component.newPagesCard.component.ColumnLayoutNewPages
import com.moegirlviewer.screen.home.component.newPagesCard.component.ListLayoutNewPages
import com.moegirlviewer.screen.home.component.newPagesCard.component.TextLayoutNewPages
import com.moegirlviewer.screen.newPages.NewPagesRouteArguments
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun NewPagesCard(
  state: NewPagesCardState
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  var viewMode by rememberSaveable { mutableStateOf<NewPagesCardViewMode?>(null) }

  LaunchedEffect(true) {
    SettingsStore.cardsHomePage.getValue { newPagesCardViewMode }
      .collect { viewMode = it }
  }

  val gotoNewPagesListPage = {
    Globals.navController.navigate(NewPagesRouteArguments(state.continueKey))
  }

  if (viewMode == null) return

  HomeCardContainer(
    icon = Icons.Filled.FiberNew,
    title = stringResource(id = R.string.newArticles),
    minHeight = 150.dp,
    loadStatus = when {
      state.status == LoadStatus.INIT_LOADING -> LoadStatus.INIT_LOADING
      state.status == LoadStatus.FAIL && state.newPageList.isEmpty() -> LoadStatus.FAIL
      else -> null
    },
    onReload = {
     scope.launch { state.reload() }
    },
    rightContent = {
      Row() {
        for (item in state.viewModes) {
          Icon(
            modifier = Modifier
              .size(34.dp)
              .padding(horizontal = 2.5.dp)
              .noRippleClickable {
                scope.launch {
                  SettingsStore.cardsHomePage.setValue { newPagesCardViewMode = item.key }
                }
              },
            imageVector = item.value,
            contentDescription = null,
            tint = if (viewMode == item.key) themeColors.primaryVariant else themeColors.text.tertiary
          )
        }
      }
    }
  ) {
    Crossfade(targetState = viewMode!!) {
      when(it) {
        NewPagesCardViewMode.TEXT -> TextLayoutNewPages(pageList = state.newPageList, onMoreButtonClick = gotoNewPagesListPage)
        NewPagesCardViewMode.LIST -> ListLayoutNewPages(pageList = state.newPageList, onMoreButtonClick = gotoNewPagesListPage)
        NewPagesCardViewMode.COLUMN -> ColumnLayoutNewPages(pageList = state.newPageList, onMoreButtonClick = gotoNewPagesListPage)
      }
    }
  }
}

class NewPagesCardState : HomeScreenCardState() {
  val viewModes = mapOf(
    NewPagesCardViewMode.TEXT to Icons.Filled.Subject,
    NewPagesCardViewMode.LIST to Icons.Filled.ViewList,
    NewPagesCardViewMode.COLUMN to Icons.Filled.ViewColumn
  )
  var newPageList by mutableStateOf(emptyList<PageProfileBean.Query.MapValue>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null

  suspend fun load() {
    status = if (status == LoadStatus.INITIAL) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    try {
      val newPagesRes = EditingRecordApi.getNewPages()
      val newPagesIds = newPagesRes.query.recentchanges.map { it.pageid }
      val pageIdKey = PageIdKey(*newPagesIds.toIntArray())
      val newPagesWithProfileRes = PageApi.getPageProfile(pageIdKey)
      newPageList = newPagesWithProfileRes.query.pages.values
        .filter { it.ns == 0 }   // 如果其中有条目被打回用户页，会出现newPages接口返回页面为条目，pagesProfile返回页面为用户页的情况，这里需要额外过滤
        .sortedBy { it.pageid }
        .reversed()
      continueKey = newPagesRes.`continue`?.rccontinue
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载最新页面卡片数据失败")
      status = LoadStatus.FAIL
    }
  }

  override suspend fun reload() = load()
}

enum class NewPagesCardViewMode {
  TEXT,
  LIST,
  COLUMN
}