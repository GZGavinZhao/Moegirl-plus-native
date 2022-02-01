package com.moegirlviewer.screen.splashPreview

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName
import com.moegirlviewer.util.SplashImageKey

@RouteName("splashPreview")
class SplashPreviewRouteArguments(
  val intiialSplashImageKey: SplashImageKey
) : RouteArguments() {
  constructor() : this(SplashImageKey._2015_4)
}