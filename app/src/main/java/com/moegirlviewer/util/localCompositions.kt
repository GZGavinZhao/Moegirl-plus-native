package com.moegirlviewer.util

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.node.Ref
import com.moegirlviewer.component.compose.composeDrawerLayout.ComposeDrawerLayoutRef

val LocalCommonDrawerRef = staticCompositionLocalOf<Ref<ComposeDrawerLayoutRef>> { error("CommonDrawer还未初始化") }