package com.moegirlviewer.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.google.accompanist.insets.LocalWindowInsets
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs

fun Modifier.sideBorder(
  side: BorderSide,
  width: Dp,
  color: Color,
): Modifier {
  return this
    .padding(
      top = if (side == BorderSide.TOP) width else 0.dp,
      start = if (side == BorderSide.LEFT) width else 0.dp,
      bottom = if (side == BorderSide.BOTTOM) width else 0.dp,
      end = if (side == BorderSide.RIGHT) width else 0.dp,
    )
    .drawBehind {
      val halfWidth = width.toPx() / 2

      val startEndPointer = when (side) {
        BorderSide.TOP -> arrayOf(
          Offset(0f, -halfWidth),
          Offset(size.width, -halfWidth)
        )
        BorderSide.LEFT -> arrayOf(
          Offset(-halfWidth, 0f),
          Offset(-halfWidth, size.height)
        )
        BorderSide.RIGHT -> arrayOf(
          Offset(size.width + halfWidth, 0f),
          Offset(size.width + halfWidth, size.height)
        )
        BorderSide.BOTTOM -> arrayOf(
          Offset(0f, size.height + halfWidth),
          Offset(size.width, size.height + halfWidth)
        )
      }

      drawLine(
        color,
        startEndPointer[0],
        startEndPointer[1],
        width.toPx()
      )
    }
}

enum class BorderSide {
  TOP, LEFT, BOTTOM, RIGHT
}

inline fun Modifier.noRippleClickable(
  crossinline onClick: () -> Unit
) = composed {
  clickable(indication = null,
    interactionSource = remember { MutableInteractionSource() }) {
    onClick()
  }
}

fun Modifier.visibility(visible: Boolean): Modifier {
  return this.alpha(if (visible) 1f else 0f)
}

fun Modifier.autoFocus(delayMs: Long = 0) = composed {
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(true) {
    delay(delayMs)
    focusRequester.requestFocus()
  }

  focusRequester(focusRequester)
}

// 使用伴奏库里的Modifier.imePadding有问题，在动画过程中不知道为什么值会比真实输入法的高度大一截，动画结束后会变为正确的值
// 这里做个限制，不允许大于真实输入法的高度
// 猜测是和导航栏有关，但是使用全面屏后用imePadding仍然会高出一小截
fun Modifier.imeBottomPadding() = composed {
  val ime = LocalWindowInsets.current.ime
  val paddingValue = min(ime.layoutInsets.bottom.toDp(), ime.bottom.toDp())
  padding(bottom = paddingValue)
}