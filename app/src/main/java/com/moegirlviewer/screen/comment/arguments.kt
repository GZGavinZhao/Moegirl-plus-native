package com.moegirlviewer.screen.comment

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("comment")
class CommentRouteArguments (
  val pageName: String,
  val pageId: Int
) : RouteArguments() {
  constructor() : this("", -1)
}