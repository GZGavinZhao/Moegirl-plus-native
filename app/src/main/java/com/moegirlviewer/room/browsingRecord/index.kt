package com.moegirlviewer.room.browsingRecord

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Entity
class BrowsingRecord(
  @PrimaryKey val pageName: String,
  @ColumnInfo val displayName: String,
  @ColumnInfo val imgUrl: String? = null,
  @ColumnInfo val date: LocalDateTime = LocalDateTime.now()
)

@Dao
abstract class BrowsingRecordDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertItem(record: BrowsingRecord)

  @Query("SELECT * FROM BrowsingRecord")
  abstract fun getAll(): Flow<List<BrowsingRecord>>

  @Delete
  abstract suspend fun deleteItem(record: BrowsingRecord)

  @Query("DELETE FROM BrowsingRecord")
  abstract suspend fun clear()
}

