package com.moegirlviewer.screen.recentChanges

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.watchList.WatchListApi
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.room.watchingPage.WatchingPage
import com.moegirlviewer.screen.recentChanges.util.RawRecentChanges
import com.moegirlviewer.screen.recentChanges.util.RecentChanges
import com.moegirlviewer.screen.recentChanges.util.processRecentChanges
import com.moegirlviewer.screen.recentChanges.util.showRecentChangesOptionsDialog
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RecentChangesScreenModel @Inject constructor() : ViewModel() {
  val lazyListState = LazyListState()
  val swipeRefreshState = SwipeRefreshState(true)
  var changesList by mutableStateOf(emptyList<ListItemRenderAdapter>())
  var status by mutableStateOf(LoadStatus.INITIAL)

  suspend fun loadList() {
    if (status == LoadStatus.LOADING) return
    status = LoadStatus.LOADING

    val isLoggedIn = AccountStore.isLoggedIn.first()
    val settings = SettingsStore.recentChanges.getValue { this }.first()
    try {
      val rawChangesData = if (settings.isWatchListMode && isLoggedIn) {
        WatchListApi.getRecentChanges(
          startISO = LocalDateTime.now().minusDays(settings.daysAgo.toLong()).format(moegirlNormalTimestampDateFormatter),
          limit = settings.totalLimit,
          includeMinor = settings.includeMinor,
          includeRobot = settings.includeRobot,
          includeLog = settings.includeLog,
        ).query.watchlist.map { RawRecentChanges(it) }
      } else {
        EditingRecordApi.getRecentChanges(
          startISO = LocalDateTime.now().minusDays(settings.daysAgo.toLong()).format(moegirlNormalTimestampDateFormatter),
          limit = settings.totalLimit,
          includeMinor = settings.includeMinor,
          includeRobot = settings.includeRobot,
          includeLog = settings.includeLog,
          excludeUser = if (settings.includeSelf) null else AccountStore.userName.first()
        ).query.recentchanges.map { RawRecentChanges(it) }
      }

      val a = processRecentChanges(rawChangesData)

      val changeListOfDays = processRecentChanges(rawChangesData)
        // 添加日期，并将所有项转化为适配器
        .flatMap {
          listOf(DateTitleItemAdapter(formatTitleDate(it[0].timestamp))) + it.map { DataItemAdapter(it) }
        }

      changesList = changeListOfDays
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载最近更改列表失败")
      status = LoadStatus.FAIL
    }
  }

  suspend fun toggleMode() {
    if (status == LoadStatus.LOADING) return
    SettingsStore.recentChanges.setValue { this.isWatchListMode = !this.isWatchListMode }
    changesList = emptyList()
    loadList()
    val isWatchListMode = SettingsStore.recentChanges.getValue { this.isWatchListMode }.first()
    val allListStr = Globals.context.getString(R.string.allList)
    val watchListStr = Globals.context.getString(R.string.watchList)
    toast(Globals.context.getString(R.string.toggleRecentChangesMode,
      if (isWatchListMode) watchListStr else allListStr
    ))
  }

  private val chineseWeeks = Gson().fromJson(Globals.context.getString(R.string.jsonArray_chineseWeeks), Array<String>::class.java)
  private fun formatTitleDate(dateISO: String): String {
    val localDateTime = parseMoegirlNormalTimestamp(dateISO)
    val yearWord = Globals.context.getString(R.string.year)
    val monthWord = Globals.context.getString(R.string.month)
    val dateWord = Globals.context.getString(R.string.date)
    val week = Globals.context.getString(R.string.week)
    val chineseWeek = chineseWeeks[localDateTime.dayOfWeek.value]
    return localDateTime.format(DateTimeFormatter.ofPattern("yyyy${yearWord}M${monthWord}d${dateWord}（$week$chineseWeek）"))
  }

  suspend fun showSettingsDialog() {
    val result = showRecentChangesOptionsDialog()
    if (result != null) {
      SettingsStore.recentChanges.setValue(result)
      loadList()
    }
  }

  override fun onCleared() {
    super.onCleared()
  }
}

sealed class ListItemRenderAdapter(
  val key: Any
)

class DateTitleItemAdapter(
  val dateTitle: String,
) : ListItemRenderAdapter(dateTitle)

class DataItemAdapter(
  val data: RecentChanges,
) : ListItemRenderAdapter(data.revid)