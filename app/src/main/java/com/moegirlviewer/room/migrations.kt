package com.moegirlviewer.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE `PageContentCache` (
        `pageName` TEXT  PRIMARY KEY  NOT NULL,
        `content`  TEXT               NOT NULL,
        `pageInfo` TEXT               NOT NULL,
        `date`     INTEGER            NOT NULL
      )
    """.trimIndent())

    database.execSQL("""
      CREATE TABLE `PageNameRedirect` (
        `redirectName`  TEXT  PRIMARY KEY  NOT NULL,
        `pageName`      TEXT               NOT NULL
      )
    """.trimIndent())
  }
}