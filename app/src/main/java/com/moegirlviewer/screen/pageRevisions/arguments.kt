package com.moegirlviewer.screen.pageRevisions

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("pageRevisions")
class PageRevisionsRouteArguments(
  val pageName: String
) : RouteArguments() {
  constructor() : this("")
}