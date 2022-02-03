package com.moegirlviewer.util

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChanged
import kotlin.math.PI
import kotlin.math.abs

// 可选消费手势的DetectTransformGestures，原版无条件消耗手势导致父组件收不到手势了(主要是和pager一起使用)
suspend fun PointerInputScope.consumptionOptionalDetectTransformGestures(
  panZoomLock: Boolean = false,
  onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Boolean
) {
  forEachGesture {
    awaitPointerEventScope {
      var rotation = 0f
      var zoom = 1f
      var pan = Offset.Zero
      var pastTouchSlop = false
      val touchSlop = viewConfiguration.touchSlop
      var lockedToPanZoom = false

      awaitFirstDown(requireUnconsumed = false)
      do {
        val event = awaitPointerEvent()
        val canceled = event.changes.any { it.positionChangeConsumed() }
        if (!canceled) {
          val zoomChange = event.calculateZoom()
          val rotationChange = event.calculateRotation()
          val panChange = event.calculatePan()

          if (!pastTouchSlop) {
            zoom *= zoomChange
            rotation += rotationChange
            pan += panChange

            val centroidSize = event.calculateCentroidSize(useCurrent = false)
            val zoomMotion = abs(1 - zoom) * centroidSize
            val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
            val panMotion = pan.getDistance()

            if (zoomMotion > touchSlop ||
              rotationMotion > touchSlop ||
              panMotion > touchSlop
            ) {
              pastTouchSlop = true
              lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
            }
          }

          if (pastTouchSlop) {
            val centroid = event.calculateCentroid(useCurrent = false)
            val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
            if (effectiveRotation != 0f ||
              zoomChange != 1f ||
              panChange != Offset.Zero
            ) {
              val isConsume = onGesture(centroid, panChange, zoomChange, effectiveRotation)
              if (isConsume) {
                event.changes.forEach {
                  if (it.positionChanged()) {
                    it.consumeAllChanges()
                  }
                }
              }
            }
          }
        }
      } while (!canceled && event.changes.any { it.pressed })
    }
  }
}