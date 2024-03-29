package com.moegirlviewer.screen.splashPreview

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun SplashPreviewScreen(arguments: SplashPreviewRouteArguments) {
  val model: SplashPreviewScreenModel = hiltViewModel()
  val splashImageList = rememberMoegirlSplashImageList()
  if (splashImageList.isEmpty()) return
  val pagerState = rememberPagerState(
    initialPage = splashImageList.indexOfFirst { it.key == arguments.intiialSplashImageKey }
  )
  var visibleInfoBar by remember { mutableStateOf(true) }

  LaunchedEffect(true) {
    model.routeArguments = arguments
  }

  LaunchedEffect(true) {
    while (true) {
      model.showAppearAnimation()
      model.showHideAnimation()
    }
  }

  Scaffold(
    modifier = Modifier
      .noRippleClickable {
        visibleInfoBar = !visibleInfoBar
      },
    bottomBar = {
      if (visibleInfoBar) ComposedFooter()
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
    ) {
      HorizontalPager(
        count = splashImageList.size,
        state = pagerState,
      ) { currentPage ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .alpha(model.contentAlpha.value)
            .clip(RectangleShape),
          contentAlignment = Alignment.BottomCenter
        ) {
          AsyncImage(
            modifier = Modifier
              .fillMaxSize()
              .scale(model.imageScale.value),
            model = rememberImageRequest(data = splashImageList[currentPage].imageData),
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

      if (visibleInfoBar) ComposedHeader(splashImageList[pagerState.currentPage])
    }
  }
}

@Composable
private fun ComposedHeader(
  currentSplashImage: MoegirlSplashImage
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val selectedSplashImages by SettingsStore.common.getValue { this.selectedSplashImages }.collectAsState(
    initial = emptyList()
  )

  StyledTopAppBar(
    title = {
      Column() {
        StyledText(
          text = currentSplashImage.season + ' ' + currentSplashImage.title,
          fontSize = 18.sp,
          color = themeColors.onPrimary
        )
        StyledText(
          text = stringResource(id = R.string.author) + "：" + currentSplashImage.author,
          fontSize = 13.sp,
          color = themeColors.onPrimary
        )
      }
    },
    actions = {
      Checkbox(
        checked = selectedSplashImages.contains(currentSplashImage.key),
        colors = CheckboxDefaults.colors(
          checkedColor = themeColors.onSecondary,
          uncheckedColor = themeColors.onSecondary,
          checkmarkColor = themeColors.secondary
        ),
        onCheckedChange = { select ->
          scope.launch {
            SettingsStore.common.setValue {
              this.selectedSplashImages = if (select) {
                this.selectedSplashImages + listOf(currentSplashImage.key)
              } else {
                this.selectedSplashImages.filter { it != currentSplashImage.key }
              }
            }
          }
        }
      )
    }
  )
}

@Composable
private fun ComposedFooter() {
  val themeColors = MaterialTheme.colors
  val splashImagesSize = rememberMoegirlSplashImageList().size
  val selectedSplashImagesSize by SettingsStore.common.getValue { this.selectedSplashImages.size }.collectAsState(
    initial = 0
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(45.dp)
      .background(themeColors.primary)
      .padding(start = 20.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    StyledText(
      text = stringResource(id = R.string.selected) + "：$selectedSplashImagesSize/$splashImagesSize",
      color = themeColors.onPrimary,
      fontSize = 16.sp
    )
  }
}