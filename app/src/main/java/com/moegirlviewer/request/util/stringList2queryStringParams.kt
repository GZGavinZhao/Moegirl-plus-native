package com.moegirlviewer.request.util

fun List<String>.toQueryStringParams(parameterName: String): List<Pair<String, String>> {
  return this.map { "$parameterName[]" to it }
}