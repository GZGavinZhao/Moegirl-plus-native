package com.moegirlviewer.screen.edit

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("edit")
class EditRouteArguments(
  val pageName: String,
  val type: EditType,
  val section: String? = null,
) : RouteArguments() {
  constructor() : this("", EditType.FULL)
}

enum class EditType {
  FULL,
  SECTION,
  NEW_PAGE
}