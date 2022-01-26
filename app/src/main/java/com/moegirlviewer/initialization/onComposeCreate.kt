package com.moegirlviewer.initialization

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.OverScrollConfiguration
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.node.Ref
import androidx.navigation.NavHostController
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.decode.SvgDecoder
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.moegirlviewer.component.commonDialog.*
import com.moegirlviewer.util.Globals

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun OnComposeCreate(
  content: @Composable (NavHostController) -> Unit
) {
  val themeColors = MaterialTheme.colors
//  val defaultTextStyle = LocalTextStyle.current
  val defaultImageLoader = LocalImageLoader.current
  val navController = rememberAnimatedNavController()
  val overScrollConfig = remember {
    OverScrollConfiguration(
      glowColor = themeColors.secondary
    )
  }
  val imageLoader = remember {
    defaultImageLoader.newBuilder()
      .componentRegistry {
        add(SvgDecoder(Globals.context))
        add(GifDecoder())
      }
      .build()
  }
  val commonAlertDialogRef = remember { Ref<CommonAlertDialogRef>() }
  val commonAlertDialog2Ref = remember { Ref<CommonAlertDialogRef>() }  // 这里为了能显示最多两个全局共用Dialog所以弄成这样了，虽然有点丑
  val commonLoadingDialogRef = remember { Ref<CommonLoadingDialogRef>() }
  val commonDatePickerDialogState = remember { CommonDatePickerDialogState() }

  LaunchedEffect(true) {
    Globals.navController = navController
    Globals.imageLoader = imageLoader
    Globals.commonAlertDialog = commonAlertDialogRef.value!!
    Globals.commonAlertDialog2 = commonAlertDialog2Ref.value!!
    Globals.commonLoadingDialog = commonLoadingDialogRef.value!!
    Globals.commonDatePickerDialog = commonDatePickerDialogState
    onComposeCreated()
  }

  CompositionLocalProvider(
    LocalImageLoader provides imageLoader,
    LocalOverScrollConfiguration provides overScrollConfig,
  ) {
    content(navController)
    CommonDatePickerDialog(state = commonDatePickerDialogState)
    CommonAlertDialog(ref = commonAlertDialogRef)
    CommonAlertDialog(ref = commonAlertDialog2Ref)
    CommonLoadingDialog(ref = commonLoadingDialogRef)
  }
}