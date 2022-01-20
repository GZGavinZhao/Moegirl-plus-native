package com.moegirlviewer.screen.browsingHistorySearch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.util.Globals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class BrowsingHistorySearchScreenModel @Inject constructor() : ViewModel() {
  var searchInputVal by mutableStateOf("")
  private var _fullList: List<BrowsingRecord>? = null
  val fullListFlow get() = flow {
    if (_fullList == null) _fullList = Globals.room.browsingRecord().getAll().first()
    emit(_fullList!!)
  }

  var searchingResult by mutableStateOf(emptyList<BrowsingRecord>())

  suspend fun search() {
    if (searchInputVal == "") {
      searchingResult = emptyList()
      return
    }

    val fullList = fullListFlow.first()
    searchingResult = fullList.filter { it.pageName.contains(searchInputVal) }
  }

  override fun onCleared() {
    super.onCleared()
  }
}