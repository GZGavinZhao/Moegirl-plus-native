package com.moegirlviewer.screen.splashPreview

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("splashPreview")
class SplashPreviewRouteArguments(
  val intiialSplashImageKey: String
) : RouteArguments() {
  constructor() : this("")
}