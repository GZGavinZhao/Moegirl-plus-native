package com.moegirlviewer.store

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.moegirlviewer.DataStoreName
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.SplashImage
import com.moegirlviewer.util.SplashImageKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 这里保存的是app中所有的设置项，不仅仅是settings页面中的设置
// 每个设置项组应该继承Settings，所有成员使用var声明，带有默认值，之后在SettingsStore中声明client
sealed class Settings

data class CommonSettings(
  var heimu: Boolean = true,
  var stopMediaOnLeave: Boolean = false,
  var syntaxHighlight: Boolean = true,
  var darkThemeBySystem: Boolean = false,
  var useSpecialCharSupportedFontInApp: Boolean = false,
  var useSpecialCharSupportedFontInArticle: Boolean = false,
  var splashImageMode: SplashImageMode = SplashImageMode.NEW,
  var selectedSplashImages: List<SplashImageKey> = listOf(SplashImageKey.values().last())
) : Settings()

enum class SplashImageMode {
  NEW,
  OFF,
  RANDOM,
  CUSTOM_RANDOM
}

data class RecentChangesSettings(
  var daysAgo: Int = 7,
  var totalLimit: Int = 500,
  var includeSelf: Boolean = true,
  var includeMinor: Boolean = true,
  var includeRobot: Boolean = false,
  var isWatchListMode: Boolean = false
) : Settings()

data class OtherSettings(
  var rejectedVersionName: String = "",
) : Settings()

object SettingsStore {
  val common = SettingsStoreClient(CommonSettings::class.java)
  val recentChanges = SettingsStoreClient(RecentChangesSettings::class.java)
  val otherSettings = SettingsStoreClient(OtherSettings::class.java)
}



private val Context.dataStore by preferencesDataStore(DataStoreName.SETTINGS.name)
private val dataStore get() = Globals.context.dataStore

class SettingsStoreClient<T : Settings>(
  private val entity: Class<out T>
) {
  private val preferencesKey = stringPreferencesKey(entity.simpleName)

  fun <SelectedValue> getValue(
    getter: (T.() -> SelectedValue)
  ) = dataStore.data.map {
    val jsonStr = it[preferencesKey]
    val entityInstance = if (jsonStr != null) Gson().fromJson(jsonStr, entity) else entity.newInstance()
    getter.invoke(entityInstance)
  }

  suspend fun setValue(setter: T.() -> Unit) {
    val jsonStr = dataStore.data.first()[preferencesKey]
    val entityInstance = if (jsonStr != null) Gson().fromJson(jsonStr, entity) else entity.newInstance()
    setter.invoke(entityInstance)
    dataStore.updateData {
      it.toMutablePreferences().apply { this[preferencesKey] = Gson().toJson(entityInstance) }
    }
  }

  suspend fun setValue(value: T) {
    dataStore.updateData {
      it.toMutablePreferences().apply { this[preferencesKey] = Gson().toJson(value) }
    }
  }
}