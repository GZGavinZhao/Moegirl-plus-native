package com.moegirlviewer.room

import android.content.Context
import androidx.room.*
import com.moegirlviewer.room.backupRecord.BackupRecord
import com.moegirlviewer.room.backupRecord.BackupRecordDao
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.room.browsingRecord.BrowsingRecordDao
import com.moegirlviewer.room.watchingPage.WatchingPage
import com.moegirlviewer.room.watchingPage.WatchingPageDao

@Database(
  entities = [
    BrowsingRecord::class,
    BackupRecord::class,
    WatchingPage::class,
 ],
  version = 1,
  exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun browsingRecord(): BrowsingRecordDao
  abstract fun backupRecord(): BackupRecordDao
  abstract fun watchingPage(): WatchingPageDao
}

fun initRoom(context: Context) = Room.databaseBuilder(
  context,
  AppDatabase::class.java,
  "main"
)
  .build()