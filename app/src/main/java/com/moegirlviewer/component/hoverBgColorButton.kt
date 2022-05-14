package com.moegirlviewer.component

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
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
  val hoverBgIndication = remember(backgroundColor) { HoverBgIndication(backgroundColor) }

  CompositionLocalProvider(
    LocalIndication provides hoverBgIndication
  ) {
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
}

class HoverBgIndication(
  val backgroundColor: Color
) : Indication {
  private class HoverBgIndicationInstance(
    private val isPressed: State<Boolean>,
    private val isHovered: State<Boolean>,
    private val isFocused: State<Boolean>,
    private val backgroundColor: Color
  ) : IndicationInstance {
    override fun ContentDrawScope.drawIndication() {
      if (isPressed.value || isHovered.value || isFocused.value) {
        drawRect(color = backgroundColor, size = size)
      }
      drawContent()
    }
  }

  @Composable
  override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
    val isPressed = interactionSource.collectIsPressedAsState()
    val isHovered = interactionSource.collectIsHoveredAsState()
    val isFocused = interactionSource.collectIsFocusedAsState()
    return remember(interactionSource) {
      HoverBgIndicationInstance(isPressed, isHovered, isFocused, backgroundColor)
    }
  }
}