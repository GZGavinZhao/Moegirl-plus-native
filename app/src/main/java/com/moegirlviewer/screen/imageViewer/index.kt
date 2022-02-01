package com.moegirlviewer.screen.imageViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.R
import com.moegirlviewer.component.compose.composePagedBigImageViews.ComposePagedBigImageViews
import com.moegirlviewer.component.styled.StyledText

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun ImageViewerScreen(
  arguments: ImageViewerRouteArguments
) {
  val model: ImageViewerScreenModel = hiltViewModel()
  val configuration = LocalConfiguration.current

  LaunchedEffect(true) {
    model.routeArguments = arguments
    model.currentImgIndex = arguments.initialIndex
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomStart
  ) {
    ComposePagedBigImageViews(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black),
      images = arguments.images,
      initialIndex = arguments.initialIndex,
      onPageChange = { model.currentImgIndex = it }
    )

    if (arguments.images.size > 1) {
      Column(
        modifier = Modifier
          .offset(20.dp, (-20).dp)
          .width((configuration.screenWidthDp * 0.6).dp),
      ) {
        StyledText(
          text = stringResource(id = R.string.gallery) + "：${model.currentImgIndex + 1} / ${arguments.images.size}",
          color = Color(0xffcccccc)
        )
        StyledText(
          text = arguments.images[model.currentImgIndex].title,
          color = Color(0xffcccccc)
        )
      }
    }
  }
}