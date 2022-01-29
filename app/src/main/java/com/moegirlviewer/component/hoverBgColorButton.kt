package com.moegirlviewer.screen.edit.tabs.wikitextEditor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.moegirlviewer.theme.background2
import kotlinx.coroutines.delay

@ExperimentalComposeUiApi
@Composable
fun HoverBgColorButton(
  backgroundColor: Color = MaterialTheme.colors.background2,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  var visibleBgColor by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .then(if (visibleBgColor) Modifier.background(backgroundColor) else Modifier)
      .pointerInput(null) {
        this.detectTapGestures(
          onPress = {
            visibleBgColor = true
            if (this.tryAwaitRelease()) onClick()
            // 延迟100毫秒，否则刚一抬手就隐藏背景会导致看不出来效果
            delay(100)
            visibleBgColor = false
          }
        )
      }
  ) {
    content()
  }
}