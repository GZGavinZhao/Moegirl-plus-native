package com.moegirlviewer.screen.imageViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.LocalImageLoader
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.moegirlviewer.R
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.component.imageViewer.ImageViewer
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.noRippleClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun ImageViewerScreen(
  arguments: ImageViewerRouteArguments
) {
  val model: ImageViewerScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val configuration = LocalConfiguration.current
  val imagePainters = arguments.images.map {
    ImageRequest.Builder(Globals.context)
      .data(it.fileUrl)
      .memoryCacheKey(it.fileUrl)
      .build()
  }

  LaunchedEffect(true) {
    model.routeArguments = arguments
    model.pagerState.scrollToPage(arguments.initialIndex)
  }

  StatusBar(
    darkIcons = false
  )

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
        imageRequest = imagePainters[currentIndex]
      )
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .offset((-70).dp, (-22).dp),
      contentAlignment = Alignment.BottomEnd
    ) {
      Icon(
        modifier = Modifier
          .size(32.dp)
          .noRippleClickable {
            scope.launch { model.shareCurrentImage() }
          },
        imageVector = Icons.Filled.Share,
        contentDescription = null,
        tint = Color(0xffeeeeee).copy(alpha = 0.5f)
      )
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .offset((-20).dp, (-20).dp),
      contentAlignment = Alignment.BottomEnd
    ) {
      Icon(
        modifier = Modifier
          .size(35.dp)
          .noRippleClickable {
            scope.launch { model.downloadCurrentImage() }
          },
        imageVector = Icons.Filled.FileDownload,
        contentDescription = null,
        tint = Color(0xffeeeeee).copy(alpha = 0.5f)
      )
    }

    Column(
      modifier = Modifier
        .offset(20.dp, (-20).dp)
        .width((configuration.screenWidthDp * 0.6).dp)
        .zIndex(1f),
      verticalArrangement = Arrangement.Center
    ) {
      if (arguments.images.size > 1) {
        StyledText(
          text = stringResource(id = R.string.gallery) + "：${model.pagerState.currentPage + 1} / ${arguments.images.size}",
          color = Color(0xffcccccc)
        )
      }
      if (arguments.images[model.pagerState.currentPage].title != "") {
        StyledText(
          text = arguments.images[model.pagerState.currentPage].title,
          color = Color(0xffcccccc)
        )
      }
    }
  }
}