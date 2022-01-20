// 和ComposeDrawerLayout配套使用的，暂时用不上了
package com.moegirlviewer.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.moegirlviewer.component.compose.composeDrawerLayout.ComposeDrawerLayoutLockMode
import com.moegirlviewer.util.LocalCommonDrawerRef

@Composable
fun EnableDrawer() {
  val commonDrawerRef = LocalCommonDrawerRef.current.value!!

  DisposableEffect(true) {
    commonDrawerRef.setLockMode(ComposeDrawerLayoutLockMode.UNLOCKED)

    onDispose {
      commonDrawerRef.setLockMode(ComposeDrawerLayoutLockMode.LOCKED)
    }
  }
}