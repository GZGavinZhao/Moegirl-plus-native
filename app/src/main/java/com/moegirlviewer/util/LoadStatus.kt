package com.moegirlviewer.util

enum class LoadStatus {
  INITIAL,
  LOADING,
  INIT_LOADING,
  SUCCESS,
  ALL_LOADED,
  EMPTY,
  FAIL;

  companion object {
    fun isCannotLoad(loadStatus: LoadStatus) = listOf(LOADING, INIT_LOADING, EMPTY, ALL_LOADED).contains(loadStatus)
  }
}

