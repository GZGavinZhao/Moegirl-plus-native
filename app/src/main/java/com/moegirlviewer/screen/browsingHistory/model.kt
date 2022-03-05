package com.moegirlviewer.screen.browsingHistory

import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BrowsingHistoryScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  var status by mutableStateOf(LoadStatus.INITIAL)
  var lists by mutableStateOf(BrowsingRecordLists())

  suspend fun refreshList() {
    status = LoadStatus.LOADING
    val room = Globals.room.browsingRecord()
    val allList = room.getAll().first()
    if (allList.isEmpty()) {
      status = LoadStatus.EMPTY
      return
    }

    val yesterdayDate = LocalDateTime.now().minusDays(1)
    val yesterdayBeginDate = yesterdayDate
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
    val yesterdayEndDate = yesterdayDate
      .withHour(23)
      .withMinute(59)
      .withSecond(59)
    val todayList = mutableListOf<BrowsingRecord>()
    val yesterdayList = mutableListOf<BrowsingRecord>()
    val earlierList = mutableListOf<BrowsingRecord>()

    for (item in allList.reversed()) {
      when {
        item.date.isAfter(yesterdayEndDate) -> todayList.add(item)
        item.date.isBefore(yesterdayBeginDate) -> earlierList.add(item)
        else -> yesterdayList.add(item)
      }
    }

    lists = BrowsingRecordLists(todayList, yesterdayList, earlierList)
    status = LoadStatus.ALL_LOADED
  }

  fun deleteRecord(record: BrowsingRecord) {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(
          text = Globals.context.getString(R.string.deleteBrowsingRecordHint)
        )
      },
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        coroutineScope.launch {
          Globals.room.browsingRecord().deleteItem(record)
          toast(Globals.context.getString(R.string.deleted))
          lists = BrowsingRecordLists(
            todayList = lists.todayList.filter { it != record },
            yesterdayList = lists.yesterdayList.filter { it != record },
            earlierList = lists.earlierList.filter { it != record }
          )
          status = LoadStatus.EMPTY
        }
      }
    ))
  }

  fun deleteAllRecords() {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(
          text = Globals.context.getString(R.string.cleanAllBrowseHistoryHint)
        )
      },
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        coroutineScope.launch {
          Globals.room.browsingRecord().clear()
          toast(Globals.context.getString(R.string.deleted))
          lists = BrowsingRecordLists()
        }
      }
    ))
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
  }
}

data class BrowsingRecordLists(
  val todayList: List<BrowsingRecord> = emptyList(),
  val yesterdayList: List<BrowsingRecord> = emptyList(),
  val earlierList: List<BrowsingRecord> = emptyList(),
)