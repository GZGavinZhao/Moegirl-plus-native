package com.moegirlviewer.screen.splashSetting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.settings.component.SettingsScreenItem
import com.moegirlviewer.screen.splashPreview.SplashPreviewRouteArguments
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun SplashSettingScreen() {
  val model: SplashSettingScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val coroutineScope = rememberCoroutineScope()
  val selectedSplashImageMode by SettingsStore.common.getValue { this.splashImageMode }.collectAsState(
    initial = SplashImageMode.NEW
  )
  val selectedSplashImages by SettingsStore.common.getValue { this.selectedSplashImages }.collectAsState(
    initial = remember { emptyList() }
  )
  val reversedSplashImageList = remember { splashImageList.reversed() }

  BackHandler(selectedSplashImageMode == SplashImageMode.CUSTOM_RANDOM && selectedSplashImages.isEmpty()) {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(text = stringResource(id = R.string.splashImageUnselectedHint))
      },
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        coroutineScope.launch {
          SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.RANDOM }
          Globals.navController.popBackStack()
        }
      }
    ))
  }

  Scaffold(
    topBar = {
      StyledTopAppBar(
        title = {
          StyledText(
            text = stringResource(id = R.string.selectSplashScreenImage),
            color = themeColors.onPrimary
          )
        }
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .verticalScroll(rememberScrollState())
    ) {
      SettingsScreenItem(
        title = stringResource(id = R.string.latest),
        innerVerticalPadding = false,
        onClick = {
          coroutineScope.launch {
            SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.NEW }
          }
        }
      ) {
        RadioButton(
          selected = selectedSplashImageMode == SplashImageMode.NEW,
          onClick = {
            coroutineScope.launch {
              SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.NEW }
            }
          }
        )
      }

      SettingsScreenItem(
        title = stringResource(id = R.string._off),
        innerVerticalPadding = false,
        onClick = {
          coroutineScope.launch {
            SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.OFF }
          }
        }
      ) {
        RadioButton(
          selected = selectedSplashImageMode == SplashImageMode.OFF,
          onClick = {
            coroutineScope.launch {
              SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.OFF }
            }
          }
        )
      }

      SettingsScreenItem(
        title = stringResource(id = R.string.random),
        innerVerticalPadding = false,
        onClick = {
          coroutineScope.launch {
            SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.RANDOM }
          }
        }
      ) {
        RadioButton(
          selected = selectedSplashImageMode == SplashImageMode.RANDOM,
          onClick = {
            coroutineScope.launch {
              SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.RANDOM }
            }
          }
        )
      }

      SettingsScreenItem(
        title = stringResource(id = R.string.customRandom),
        innerVerticalPadding = false,
        visibleBorder = false,
        onClick = {
          coroutineScope.launch {
            SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.CUSTOM_RANDOM }
          }
        }
      ) {
        RadioButton(
          selected = selectedSplashImageMode == SplashImageMode.CUSTOM_RANDOM,
          onClick = {
            coroutineScope.launch {
              SettingsStore.common.setValue { this.splashImageMode = SplashImageMode.CUSTOM_RANDOM }
            }
          }
        )
      }

      FlowRow(
        modifier = Modifier
          .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
          .fillMaxWidth(),
        mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
        lastLineMainAxisAlignment = MainAxisAlignment.Start,
        mainAxisSpacing = 5.dp,
        crossAxisSpacing = 5.dp
      ) {
        for (item in reversedSplashImageList) {
          ImageItem(
            splashImage = item,
            visiblePreviewButton = selectedSplashImageMode == SplashImageMode.CUSTOM_RANDOM,
            selected = selectedSplashImages.contains(item.key),
            onPreviewClick = {
              Globals.navController.navigate(SplashPreviewRouteArguments(
                intiialSplashImageKey = item.key
              ))
            },
            onClick = {
              coroutineScope.launch {
                SettingsStore.common.setValue {
                  if (this.splashImageMode != SplashImageMode.CUSTOM_RANDOM) {
                    this.splashImageMode = SplashImageMode.CUSTOM_RANDOM
                    if (!this.selectedSplashImages.contains(item.key)) {
                      this.selectedSplashImages = this.selectedSplashImages + listOf(item.key)
                    }
                  } else {
                    this.selectedSplashImages = if (this.selectedSplashImages.contains(item.key)) {
                      this.selectedSplashImages.filter { it != item.key }
                    } else {
                      this.selectedSplashImages + listOf(item.key)
                    }
                  }
                }
              }
            }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ImageItem(
  splashImage: SplashImage,
  visiblePreviewButton: Boolean,
  selected: Boolean,
  onPreviewClick: () -> Unit,
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors
  val animatedBorderWidth by animateDpAsState(
    if (selected && visiblePreviewButton) 4.dp else 0.dp
  )

  Box(
    modifier = Modifier
      .width(120.dp)
      .height(200.dp)
      .border(
        width = animatedBorderWidth,
        color = themeColors.primaryVariant
      )
  ) {
    AsyncImage(
      modifier = Modifier
        .fillMaxSize()
        .clickable { onClick() },
      model = rememberImageRequest(data = splashImage.imageData),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .offset(5.dp, (-7).dp),
      contentAlignment = Alignment.TopEnd
    ) {
      AnimatedVisibility(
        visible = visiblePreviewButton,
        enter = scaleIn(),
        exit = scaleOut()
      ) {
        IconButton(
          onClick = onPreviewClick
        ) {
          Icon(
            modifier = Modifier
              .size(24.dp)
              .alpha(0.5f),
            imageVector = ImageVector.vectorResource(id = R.drawable.eye),
            contentDescription = null,
            tint = themeColors.primaryVariant.copy(alpha = 0.8f),
          )
        }
      }
    }
  }
}

enum class SplashImageMode {
  NEW,
  OFF,
  RANDOM,
  CUSTOM_RANDOM
}