package com.moegirlviewer.room.pageNameRedirect

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
class PageNameRedirect(
  @PrimaryKey val redirectName: String,
  @ColumnInfo val pageName: String,
)

@Dao
abstract class PageNameRedirectDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertItem(record: PageNameRedirect)

  @Query("SELECT * FROM PageNameRedirect WHERE redirectName = :redirectName")
  abstract fun getItem(redirectName: String): Flow<PageNameRedirect?>

  @Query("DELETE FROM WatchingPage")
  abstract suspend fun clear()
}