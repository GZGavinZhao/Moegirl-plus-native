package com.moegirlviewer.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R

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
    Image(
      modifier = Modifier
        .fillMaxSize()
        .scale(state.imageScale.value),
      painter = painterResource(R.drawable.splash_image),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height((LocalConfiguration.current.screenHeightDp * 0.15).dp)
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
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stringResource(id = R.string.app_name),
        fontSize = 24.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )
    }
  }
}