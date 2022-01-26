package com.moegirlviewer.room.pageContentCache

import androidx.room.*
import com.google.gson.Gson
import com.moegirlviewer.api.page.bean.PageContentResBean
import com.moegirlviewer.api.page.bean.PageInfoResBean
import com.moegirlviewer.util.Globals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

@Entity
class PageContentCache(
  @PrimaryKey val pageName: String,
  @ColumnInfo val content: PageContentResBean,
  @ColumnInfo val pageInfo: PageInfoResBean.Query.MapValue,
  @ColumnInfo val date: LocalDateTime = LocalDateTime.now()
)

@Dao
abstract class PageContentCacheDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  internal abstract suspend fun insertItem(record: PageContentCache)

  @Query("SELECT * FROM PageContentCache WHERE pageName = :pageName")
  internal abstract fun getItem(pageName: String): Flow<PageContentCache?>

  @Delete
  abstract suspend fun removeItem(record: PageContentCache)

  @Query("DELETE FROM PageContentCache")
  abstract suspend fun clear()

  suspend fun getCache(pageName: String): Flow<PageContentCache?> {
    val truePageName = Globals.room.pageNameRedirect().getItem(pageName).first()?.redirectName ?: pageName
    return getItem(truePageName)
  }
}