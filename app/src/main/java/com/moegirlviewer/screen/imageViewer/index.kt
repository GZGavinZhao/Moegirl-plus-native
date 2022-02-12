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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.moegirlviewer.R
import com.moegirlviewer.component.imageViewer.ImageViewer
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
  val imagePainters = arguments.images.map { rememberImagePainter(it.fileUrl) }

  LaunchedEffect(true) {
    model.routeArguments = arguments
    model.pagerState.scrollToPage(arguments.initialIndex)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = Alignment.BottomStart
  ) {
    HorizontalPager(
      state = model.pagerState,
      count = arguments.images.size
    ) { currentIndex ->
      ImageViewer(
        modifier = Modifier
          .fillMaxSize(),
        scaleRange = 0.8f..4f,
        painter = imagePainters[currentIndex]
      )
    }

    if (arguments.images.size > 1) {
      Column(
        modifier = Modifier
          .offset(20.dp, (-20).dp)
          .width((configuration.screenWidthDp * 0.6).dp)
          .zIndex(1f),
      ) {
        StyledText(
          text = stringResource(id = R.string.gallery) + "：${model.pagerState.currentPage + 1} / ${arguments.images.size}",
          color = Color(0xffcccccc)
        )
        StyledText(
          text = arguments.images[model.pagerState.currentPage].title,
          color = Color(0xffcccccc)
        )
      }
    }
  }
}