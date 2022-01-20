package com.moegirlviewer.screen.commentReply

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("commentReply")
class CommentReplyRouteArguments (
  val pageId: Int,
  val commentId: String,
) : RouteArguments() {
  constructor() : this(-1, "")
}