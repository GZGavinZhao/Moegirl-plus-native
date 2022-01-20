package com.moegirlviewer.screen.imageViewer

import com.moegirlviewer.component.articleView.MoegirlImage
import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("imageViewer")
class ImageViewerRouteArguments (
  val images: List<MoegirlImage>,
  val initialIndex: Int = 1,
) : RouteArguments() {
  constructor() : this(
    images = emptyList()
  )
}