package com.moegirlviewer.component.imageViewer

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.ImagePainter
import coil.request.ImageRequest
import com.moegirlviewer.util.consumptionOptionalDetectTransformGestures
import com.moegirlviewer.util.noRippleClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, coil.annotation.ExperimentalCoilApi::class)
@Composable
fun ImageViewer(
  modifier: Modifier = Modifier,
  imageRequest: ImageRequest,
  scaleRange: ClosedRange<Float> = 0.8f..2.5f,
  progressIndicator: @Composable () -> Unit = {
    DefaultImageViewerProgressIndicator()
  }
) {
  val scope = rememberCoroutineScope()
  var containerWidth by remember { mutableStateOf(0) }
  var containerHeight by remember { mutableStateOf(0) }
  val translateX = remember { Animatable(0f) }
  val translateY = remember { Animatable(0f) }
  val scale = remember { Animatable(1f) }
  var imageHeight by remember { mutableStateOf(0) }
  var imageLoaded by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .clip(RectangleShape)
        .then(modifier)
        .onGloballyPositioned {
          containerWidth = it.size.width
          containerHeight = it.size.height
        }
        .pointerInput(Unit) {
          this.detectTapGestures(
            onDoubleTap = {
              if (scale.value != 1f) {
                scope.launch { scale.animateTo(1f) }
                scope.launch { translateX.animateTo(0f) }
                scope.launch { translateY.animateTo(0f) }
              } else {
                scope.launch {
                  scale.animateTo((scaleRange.endInclusive + scaleRange.start) / 2)
                }
              }
            }
          )
        }
        .pointerInput(Unit) {
          this.consumptionOptionalDetectTransformGestures { centroid, pan, zoom, rotation ->
            val maxTranslateX = java.lang.Float.max(0f, containerWidth * (scale.value - 1) * 0.5f)
            val maxTranslateY =
              java.lang.Float.max(0f, (imageHeight * scale.value - containerHeight) * 0.5f)

            var nextScale = scale.value
            var nextTranslateX = translateX.value
            var nextTranslateY = translateY.value

            if (nextScale > 1) {
              nextTranslateX = (nextTranslateX + pan.x).coerceIn(-maxTranslateX..maxTranslateX)
              nextTranslateY = (nextTranslateY + pan.y).coerceIn(-maxTranslateY..maxTranslateY)
            }

            nextScale = (nextScale * zoom).coerceIn(scaleRange)

            if (zoom < 0) {
              if (nextScale > 1) {
                nextTranslateX *= zoom
                nextTranslateY *= zoom
              } else {
                nextTranslateX = 0f
                nextTranslateY = 0f
              }
            }

            scope.launch { scale.snapTo(nextScale) }
            scope.launch { translateX.snapTo(nextTranslateX) }
            scope.launch { translateY.snapTo(nextTranslateY) }

            nextScale > 1 && nextTranslateX != -maxTranslateX && nextTranslateX != maxTranslateX
          }
        },
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        modifier = Modifier
          .fillMaxWidth()
          .graphicsLayer(
            scaleX = scale.value,
            scaleY = scale.value,
            translationX = translateX.value,
            translationY = translateY.value
          )
          .onGloballyPositioned { imageHeight = it.size.height },
        contentDescription = null,
        model = imageRequest,
        contentScale = ContentScale.FillWidth,
        onSuccess = { imageLoaded = true }
      )
    }

    if (imageLoaded) {
      Box(
        modifier = Modifier
          .matchParentSize()
          .noRippleClickable { },
        contentAlignment = Alignment.Center
      ) {
        progressIndicator()
      }
    }
  }
}

@Composable
private fun DefaultImageViewerProgressIndicator() {
  CircularProgressIndicator(
    modifier = Modifier
      .size(50.dp),
    color = Color.White,
    strokeWidth = 4.dp
  )
}