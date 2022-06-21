package com.moegirlviewer.screen.newPages

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("newPages")
class NewPagesRouteArguments(
  val continueKey: String? = null
) : RouteArguments() {
  constructor() : this("")
}