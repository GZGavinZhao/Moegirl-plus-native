package com.moegirlviewer.screen.randomPages.component

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.moegirlviewer.R
import com.moegirlviewer.util.Globals
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun RandomPageActionButton(
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  var iconList by remember { mutableStateOf(listOf(
    AppCompatResources.getDrawable(Globals.context, R.drawable.dice_5_outline),
    AppCompatResources.getDrawable(Globals.context, R.drawable.dice_1_outline),
    AppCompatResources.getDrawable(Globals.context, R.drawable.dice_2_outline),
    AppCompatResources.getDrawable(Globals.context, R.drawable.dice_3_outline),
    AppCompatResources.getDrawable(Globals.context, R.drawable.dice_4_outline),
    AppCompatResources.getDrawable(Globals.context, R.drawable.dice_6_outline),
  )) }

  val sizeAnimationValue = remember { Animatable(0f) }
  val rotationAnimationValue = remember { Animatable(0f) }
  var rotationAnimationType by remember { mutableStateOf(RotationAnimationType.X) }
  val usingIcon = iconList[floor(sizeAnimationValue.value * 6 * 1 % 6).toInt()]
  val animatedScale = 1f + (0.3f * sizeAnimationValue.value)

  fun showAnimation() = scope.launch {
    iconList = iconList.shuffled()
    rotationAnimationType = RotationAnimationType.values().random()
    val duration = 150

    launch {
      launch {
        rotationAnimationValue.animateTo(
          targetValue = rotationAnimationValue.value + 180f,
          animationSpec = tween(
            durationMillis = duration
          )
        )
      }
      launch {
        sizeAnimationValue.animateTo(
          targetValue = 1f,
          animationSpec = tween(
            durationMillis = duration,
          )
        )
      }
    }.join()

    launch {
      launch {
        rotationAnimationValue.animateTo(
          targetValue = rotationAnimationValue.value + 360f,
          animationSpec = tween(
            durationMillis = duration
          )
        )
      }
      launch {
        sizeAnimationValue.animateTo(
          targetValue = 0f,
          animationSpec = tween(
            durationMillis = duration,
          )
        )
      }
    }.join()
  }

  Surface(
    modifier = Modifier
      .size(60.dp)
      .graphicsLayer(
        scaleX = animatedScale,
        scaleY = animatedScale,
      ),
    elevation = 1.dp,
    shape = CircleShape
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(themeColors.primary)
        .clickable {
          if (rotationAnimationValue.isRunning) return@clickable
          showAnimation()
          onClick()
        },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        modifier = Modifier
          .size(35.dp)
          .then(when(rotationAnimationType) {
            RotationAnimationType.X -> Modifier.graphicsLayer(rotationX = rotationAnimationValue.value)
            RotationAnimationType.Y -> Modifier.graphicsLayer(rotationY = rotationAnimationValue.value)
            RotationAnimationType.Z -> Modifier.graphicsLayer(rotationZ = rotationAnimationValue.value)
          }),
        painter = rememberAsyncImagePainter(usingIcon),
        contentDescription = null,
        tint = if (themeColors.isLight) themeColors.onPrimary else themeColors.secondary
      )
    }
  }
}

private enum class RotationAnimationType {
  X, Y, Z
}