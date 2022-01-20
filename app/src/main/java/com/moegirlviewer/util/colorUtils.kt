package com.moegirlviewer.extants

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun Color.toCssRgbaString(): String {
  fun toByteValue(value: Float): Int = (value * 255).roundToInt()
  val (r, g, b) = listOf(toByteValue(red), toByteValue(green), toByteValue(blue))
  return "rgba($r, $g, $b, $alpha)"
}

fun Color.darken(rate: Float): Color {
  fun compute(value: Float): Float = min(max(value * (1 - rate), 0F), 255F)
  return copy(alpha, compute(red), compute(green), compute(blue))
}

fun Color.lighten(rate: Float): Color {
  fun compute(value: Float): Float = min(max(value * (1 + rate), 0F), 255F)
  return copy(alpha, compute(red), compute(green), compute(blue))
}