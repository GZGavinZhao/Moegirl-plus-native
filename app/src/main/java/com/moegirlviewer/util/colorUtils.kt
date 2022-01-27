package com.moegirlviewer.util

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

fun Color.toCssRgbaString(): String {
  fun toByteValue(value: Float): Int = (value * 255).roundToInt()
  val (r, g, b) = listOf(toByteValue(red), toByteValue(green), toByteValue(blue))
  return "rgba($r, $g, $b, $alpha)"
}

fun Color.darken(rate: Float): Color {
  fun compute(value: Float): Float = (value * (1 - rate)).coerceIn(0f, 1f)
  return copy(alpha, compute(red), compute(green), compute(blue))
}

fun Color.lighten(rate: Float): Color {
  fun compute(value: Float): Float = (value * (1 + rate)).coerceIn(0f, 1f)
  return copy(alpha, compute(red), compute(green), compute(blue))
}