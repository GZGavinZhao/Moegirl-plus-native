package com.moegirlviewer.room

import android.content.Context
import androidx.room.*
import com.moegirlviewer.room.backupRecord.BackupRecord
import com.moegirlviewer.room.backupRecord.BackupRecordDao
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.room.browsingRecord.BrowsingRecordDao
import com.moegirlviewer.room.pageContentCache.PageContentCache
import com.moegirlviewer.room.pageContentCache.PageContentCacheDao
import com.moegirlviewer.room.pageNameRedirect.PageNameRedirect
import com.moegirlviewer.room.pageNameRedirect.PageNameRedirectDao
import com.moegirlviewer.room.watchingPage.WatchingPage
import com.moegirlviewer.room.watchingPage.WatchingPageDao

@Database(
  entities = [
    BrowsingRecord::class,
    BackupRecord::class,
    WatchingPage::class,
    PageContentCache::class,
    PageNameRedirect::class
 ],
  version = 2,
  exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun browsingRecord(): BrowsingRecordDao
  abstract fun backupRecord(): BackupRecordDao
  abstract fun watchingPage(): WatchingPageDao
  abstract fun pageContentCache(): PageContentCacheDao
  abstract fun pageNameRedirect(): PageNameRedirectDao
}

fun initRoom(context: Context) = Room.databaseBuilder(
  context,
  AppDatabase::class.java,
  "main"
)
  .addMigrations(MIGRATION_1_2)
  .build()