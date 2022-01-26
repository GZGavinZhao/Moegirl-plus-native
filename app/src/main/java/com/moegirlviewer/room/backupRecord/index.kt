package com.moegirlviewer.room.backupRecord

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Entity(primaryKeys = ["type", "backupId"])
class BackupRecord(
  @ColumnInfo val type: BackupRecordType,
  @ColumnInfo val backupId: String,  // 内容签名，同一type下签名唯一
  @ColumnInfo val content: String,
  @ColumnInfo val date: LocalDateTime = LocalDateTime.now()
)

@Dao
interface BackupRecordDao {
  @Query("SELECT * FROM BackupRecord WHERE type = :type AND backupId = :backupId")
  fun getItem(type: BackupRecordType, backupId: String): Flow<BackupRecord?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(record: BackupRecord)

  @Delete
  suspend fun deleteItem(record: BackupRecord)

  @Query("DELETE FROM BrowsingRecord")
  suspend fun clear()
}

enum class BackupRecordType {
  EDIT_CONTENT
}