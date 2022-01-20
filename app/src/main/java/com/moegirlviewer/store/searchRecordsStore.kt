package com.moegirlviewer.store

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.moegirlviewer.DataStoreName
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.ProguardIgnore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object SearchRecordsStore {
  private val Context.dataStore by preferencesDataStore(DataStoreName.SEARCH_RECORDS.name)
  private val dataStore get() = Globals.context.dataStore
  private val dataStoreKeys = object {
    val searchRecords = stringPreferencesKey("searchRecords")
  }

  val searchRecords get() = dataStore.data.map {
    val array = Gson().fromJson(it[dataStoreKeys.searchRecords], Array<SearchRecord>::class.java) ?: emptyArray()
    listOf(*array)
  }

  private suspend fun writeSearchRecords(value: List<SearchRecord>) {
    dataStore.updateData {
      it.toMutablePreferences().apply {
        this[dataStoreKeys.searchRecords] = Gson().toJson(value)
      }
    }
  }

  suspend fun addRecord(record: SearchRecord) {
    val currentValue = searchRecords.first().toMutableList()
    val newValue = currentValue.apply {
      this.removeIf { it.keyword == record.keyword }
      this.add(0, record)
    }
    writeSearchRecords(newValue)
  }

  suspend fun removeRecord(keyword: String) {
    val currentValue = searchRecords.first().toMutableList()
    val newValue = currentValue.apply {
      this.removeIf { it.keyword == keyword }
    }
    writeSearchRecords(newValue)
  }

  suspend fun clearRecord() {
    writeSearchRecords(emptyList())
  }
}

@ProguardIgnore
class SearchRecord(
  val keyword: String,
  val isPageName: Boolean
)