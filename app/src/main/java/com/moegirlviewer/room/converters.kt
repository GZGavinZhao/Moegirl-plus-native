package com.moegirlviewer.room

import androidx.annotation.InspectableProperty
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.moegirlviewer.api.page.bean.PageContentResBean
import com.moegirlviewer.api.page.bean.PageInfoResBean
import com.moegirlviewer.room.backupRecord.BackupRecordType
import com.moegirlviewer.util.toEpochMilli
import com.moegirlviewer.util.toLocalDateTime
import java.time.LocalDateTime

class RoomConverters {
  // 通用类型转换
  @TypeConverter fun timestampToDate(value: Long) = value.toLocalDateTime()
  @TypeConverter fun timestampFromDate(value: LocalDateTime) = value.toEpochMilli()

  // 枚举类转换
  @TypeConverter fun enumEntryToString(value: InspectableProperty.EnumEntry) = value.name
  @TypeConverter fun backupRecordTypeFromString(value: String) = BackupRecordType.valueOf(value)

  // Bean转换
  @TypeConverter fun pageContentResBeanToJson(value: PageContentResBean) = Gson().toJson(value)
  @TypeConverter fun pageContentResBeanFromJson(value: String) = Gson().fromJson(value, PageContentResBean::class.java)

  @TypeConverter fun pageInfoToJson(value:  PageInfoResBean.Query.MapValue) = Gson().toJson(value)
  @TypeConverter fun pageInfoFromJson(value: String) = Gson().fromJson(value, PageInfoResBean.Query.MapValue::class.java)
}