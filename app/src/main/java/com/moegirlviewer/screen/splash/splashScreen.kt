package com.moegirlviewer.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.util.isMoegirl

// 这个页面本身不在路由中
@Composable
fun SplashScreen(
  state: SplashScreenState
) {

  Box(
    modifier = Modifier
      .fillMaxSize()
      .alpha(state.contentAlpha.value),
    contentAlignment = Alignment.BottomCenter
  ) {
    AsyncImage(
      modifier = Modifier
        .fillMaxSize()
        .scale(state.imageScale.value),
      model = state.splashImage.imageData,
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Spacer(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.2f)
        .background(
          brush = remember {
            Brush.verticalGradient(
              listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.5f)
              )
            )
          }
        ),
    )

    if (isMoegirl()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .offset(y = (-70).dp),
        contentAlignment = Alignment.BottomCenter
      ) {
        Image(
          modifier = Modifier
            .width(174.dp)
            .height(55.dp),
          painter = painterResource(id = R.drawable.site_name),
          contentDescription = null
        )
      }
    }
  }
}