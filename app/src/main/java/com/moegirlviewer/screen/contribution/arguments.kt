package com.moegirlviewer.screen.contribution

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("contribution")
class ContributionRouteArguments(
  val userName: String
) : RouteArguments() {
  constructor() : this("")
}
