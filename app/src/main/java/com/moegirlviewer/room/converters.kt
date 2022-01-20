package com.moegirlviewer.room

import androidx.annotation.InspectableProperty
import androidx.datastore.preferences.protobuf.Enum
import androidx.room.TypeConverter
import com.moegirlviewer.room.backupRecord.BackupRecordType
import com.moegirlviewer.util.toEpochMilli
import com.moegirlviewer.util.toLocalDateTime
import java.time.LocalDateTime

class RoomConverters {
  @TypeConverter
  fun timestampToDate(value: Long?): LocalDateTime? {
    return value?.toLocalDateTime()
  }

  @TypeConverter
  fun dateToTimestamp(date: LocalDateTime?): Long? {
    return date?.toEpochMilli()
  }

  @TypeConverter
  fun enumEntryToString(enumEntry: InspectableProperty.EnumEntry) = enumEntry.name
  @TypeConverter fun stringToBackupRecordType(name: String) = BackupRecordType.valueOf(name)
}