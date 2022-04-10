package com.moegirlviewer.util

import android.app.Activity
import android.view.View


fun Activity.useFreeStatusBarLayout() {
  // 删除默认的顶部状态栏高度偏移
  window.decorView.systemUiVisibility =
    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
}

fun Activity.useFullScreenLayout() {
  window.decorView.systemUiVisibility =
    View.SYSTEM_UI_FLAG_FULLSCREEN or
    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
    View.SYSTEM_UI_FLAG_IMMERSIVE or
    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
}