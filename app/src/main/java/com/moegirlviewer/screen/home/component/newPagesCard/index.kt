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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.moegirlviewer.R
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.editingRecord.bean.NewPagesBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.screen.home.component.HomeCardContainer
import com.moegirlviewer.screen.home.component.newPagesCard.component.ColumnLayoutNewPages
import com.moegirlviewer.screen.home.component.newPagesCard.component.ListLayoutNewPages
import com.moegirlviewer.screen.home.component.newPagesCard.component.TextLayoutNewPages
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.printRequestErr
import kotlinx.coroutines.launch

@Composable
fun NewPagesCard(
  state: NewPagesCardState
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val viewMode by SettingsStore.cardsHomePage.getValue { newPagesCardViewMode }.collectAsState(
    initial = NewPagesCardViewMode.LIST
  )

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
    Crossfade(targetState = viewMode) {
      when(it) {
        NewPagesCardViewMode.TEXT -> TextLayoutNewPages(pageList = state.newPageList)
        NewPagesCardViewMode.LIST -> ListLayoutNewPages(pageList = state.newPageList)
        NewPagesCardViewMode.COLUMN -> ColumnLayoutNewPages(pageList = state.newPageList)
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
  var newPageList by mutableStateOf(emptyList<NewPagesBean.Query.MapValue>())
  var status by mutableStateOf(LoadStatus.INITIAL)
  var continueKey: String? = null

  suspend fun loadNext(reload: Boolean = false) {
    if (LoadStatus.isCannotLoad(status)) return
    if (reload) {
      status = LoadStatus.INITIAL
      continueKey = null
    }
    status = if (status == LoadStatus.INITIAL) LoadStatus.INIT_LOADING else LoadStatus.LOADING
    try {
      val res = EditingRecordApi.getNewPages(continueKey)
      newPageList = (if (reload) emptyList() else newPageList) + res.query.pages
        .filter { it.key > -1 }
        .values.sortedBy { it.pageid }
      continueKey = res.`continue`.grccontinue
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载最新页面卡片数据失败")
      status = LoadStatus.FAIL
    }
  }

  override suspend fun reload() = loadNext(true)
}

enum class NewPagesCardViewMode {
  TEXT,
  LIST,
  COLUMN
}