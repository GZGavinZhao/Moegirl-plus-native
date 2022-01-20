package com.moegirlviewer.screen.searchResult

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("searchResult")
class SearchResultRouteArguments (
  val keyword: String
) : RouteArguments() {
  constructor() : this("")
}