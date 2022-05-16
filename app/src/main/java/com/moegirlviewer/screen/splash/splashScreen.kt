package com.moegirlviewer.screen.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.moegirlviewer.R
import com.moegirlviewer.compable.DoSideEffect
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.compable.statusBarLocked
import com.moegirlviewer.util.*
import kotlinx.coroutines.delay

// 这个页面本身不在路由中
@Composable
fun SplashScreen(
  state: SplashScreenState = remember { SplashScreenState() }
) {
  val themeColors = MaterialTheme.colors

  LaunchedEffect(true) {
    if (state.isShowSplashScreen()) {
      statusBarLocked = true
      state.splashImage = state.getUsingSplashImage()
      state.showAppearAnimation()
      state.visible = false
      statusBarLocked = false
      delay(200)
      Globals.activity.useFreeStatusBarLayout()
    } else {
      Globals.activity.useFreeStatusBarLayout()
      state.visible = false
    }
  }

  AnimatedVisibility(
    visible = state.visible,
    enter = fadeIn(snap()),
    exit = fadeOut(tween(200))
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(themeColors.background),
      contentAlignment = Alignment.BottomCenter
    ) {
      var imageReady by remember { mutableStateOf(false) }

      if (state.splashImage != null) {
        AsyncImage(
          modifier = Modifier
            .fillMaxSize()
            .scale(state.imageScale.value),
          model = rememberImageRequest(data = state.splashImage!!.imageData),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          onSuccess = { imageReady = true }
        )
      }

      if (imageReady) {
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
      }

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
}