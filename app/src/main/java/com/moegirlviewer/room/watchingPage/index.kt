package com.moegirlviewer.room.watchingPage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
class WatchingPage(
  @PrimaryKey val pageName: String,
)

@Dao
abstract class WatchingPageDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertItem(vararg record: WatchingPage)

  @Query("SELECT * FROM WatchingPage")
  abstract fun getAll(): Flow<List<WatchingPage>>

  @Delete
  abstract suspend fun deleteItem(record: WatchingPage)

  @Query("DELETE FROM WatchingPage")
  abstract suspend fun clear()
}
