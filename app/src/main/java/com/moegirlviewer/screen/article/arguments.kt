package com.moegirlviewer.screen.article

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("article")
class ArticleRouteArguments (
  val pageName: String? = null,
  val pageId: Int? = null,
  val displayName: String? = null,
  val anchor: String? = null,
  val revId: Int? = null,  // 修订版本id，传入会加载历史版本
  val readingRecord: ReadingRecord? = null  // 阅读记录，传入这个时其他参数都不应传入，这个将加载记录中的页面以及滚动条进度
) : RouteArguments()