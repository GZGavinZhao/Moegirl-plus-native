package com.moegirlviewer.screen.category

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("category")
class CategoryRouteArguments (
  val categoryName: String,
  val parentCategories: List<String>? = null,
  val categoryExplainPageName: String? = null
) : RouteArguments() {
  constructor() : this("")
}