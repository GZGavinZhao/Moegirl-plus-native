package com.moegirlviewer.screen.compare

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

sealed class CompareRouteArguments() : RouteArguments()

@RouteName("comparePage")
class ComparePageRouteArguments(
  val fromRevId: Int,
  val toRevId: Int? = null,
  val pageName: String,
) : CompareRouteArguments() {
  constructor() : this(-1, -1, "")
}

@RouteName("compareText")
class CompareTextRouteArguments(
  val formText: String,
  val toText: String
) : CompareRouteArguments() {
  constructor() : this("", "")
}