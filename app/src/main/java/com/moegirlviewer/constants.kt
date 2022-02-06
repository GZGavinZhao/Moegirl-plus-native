package com.moegirlviewer

class SourceConstants(
  val source: DataSource,
  val apiUrl: String,
  val avatarUrl: String,
  val shareUrl: String,
  val registerUrl: String,
  val appDownloadUrl: String,
  val filePrefix: String
)

val sourceConstants = when (BuildConfig.FLAVOR_source) {
  "moegirl" -> SourceConstants(
    source = DataSource.MOEGIRL,
    apiUrl = "https://zh.moegirl.org.cn/api.php",
    avatarUrl = "https://commons.moegirl.org.cn/extensions/Avatar/avatar.php?user=",
    shareUrl = "https://mzh.moegirl.org.cn/index.php?curid=",
    registerUrl = "https://mzh.moegirl.org.cn/index.php?title=Special:创建账户",
    appDownloadUrl = "https://www.coolapk.com/apk/247471",
    filePrefix = "File:"
  )

  "hmoe" -> SourceConstants(
    source = DataSource.HMOE,
    apiUrl = "https://www.hmoegirl.com/api.php",
    avatarUrl = "https://www.hmoegirl.com/extensions/Avatar/avatar.php?user=",
    shareUrl = "https://www.hmoegirl.com/index.php?curid=",
    registerUrl = "https://www.hmoegirl.com/index.php?title=%E7%89%B9%E6%AE%8A:%E5%88%9B%E5%BB%BA%E8%B4%A6%E6%88%B7&returnto=Mainpage",
    appDownloadUrl = "",
    filePrefix = "文件:"
  )

  else -> error("数据源'${BuildConfig.FLAVOR}'的常量集未配置！")
}

object Constants {
  val source = sourceConstants.source
  val apiUrl = sourceConstants.apiUrl
  val avatarUrl = sourceConstants.avatarUrl
  val shareUrl = sourceConstants.shareUrl
  val registerUrl = sourceConstants.registerUrl
  val appDownloadUrl = sourceConstants.appDownloadUrl
  val filePrefix = sourceConstants.filePrefix

  val topAppBarHeight = 56
  val articleCacheDirPath = "article_cache"
  val targetStore = if (BuildConfig.FLAVOR_targetStore == "common")
    TargetStore.COMMON else TargetStore.FDROID
}

enum class DataSource(val code: String) {
  MOEGIRL("moegirl"),
  HMOE("hmoe")
}

enum class DataStoreName {
  ACCOUNT,
  SEARCH_RECORDS,
  SETTINGS,
}

enum class TargetStore {
  COMMON,
  FDROID
}