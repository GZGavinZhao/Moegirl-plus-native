package com.moegirlviewer.room.watchingPage

import androidx.room.*
import com.moegirlviewer.room.pageContentCache.PageContentCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Entity
class WatchingPage(
  @PrimaryKey val pageName: String,
)

@Dao
abstract class WatchingPageDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertItem(vararg record: WatchingPage)

  @Query("SELECT * FROM WatchingPage WHERE pageName = :pageName")
  internal abstract fun getItem(pageName: String): Flow<WatchingPage?>

  @Query("SELECT * FROM WatchingPage")
  abstract fun getAll(): Flow<List<WatchingPage>>

  @Delete
  abstract suspend fun deleteItem(record: WatchingPage)

  @Query("DELETE FROM WatchingPage")
  abstract suspend fun clear()

  suspend fun exists(pageName: String): Boolean {
    val foundItem = getItem(pageName).first()
    return foundItem != null
  }
}
