package com.moegirlviewer.screen.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.moegirlviewer.R
import com.moegirlviewer.component.customDrawer.CustomDrawer
import com.moegirlviewer.component.customDrawer.CustomDrawerState
import com.moegirlviewer.screen.drawer.component.CommonDrawerBody
import com.moegirlviewer.screen.drawer.component.CommonDrawerFooter
import com.moegirlviewer.screen.drawer.component.CommonDrawerHeader

@ExperimentalMaterialApi
@Composable
fun CommonDrawer(
  state: CommonDrawerState = remember { CommonDrawerState() },
  content: @Composable () -> Unit
) {
  val themeColors = MaterialTheme.colors
  val configuration = LocalConfiguration.current

  CustomDrawer(
    state = state.customDrawerState,
    width = (configuration.screenWidthDp * 0.8).dp,
    drawerContent = {
      Column(
        modifier = Modifier
          .background(color = themeColors.background),
      ) {
        CommonDrawerHeader(state)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          Image(
            modifier = Modifier
              .matchParentSize(),
            painter = painterResource(R.drawable.drawer_bg),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.tint(
              color = if (themeColors.isLight)
                Color.White.copy(alpha = 0.8f) else
                Color.Black.copy(alpha = 0.7f)
              ,
              blendMode = if (themeColors.isLight) BlendMode.Lighten else BlendMode.Darken
            )
          )

          Column(
            modifier = Modifier
              .matchParentSize()
          ) {
            CommonDrawerBody(
              modifier = Modifier
                .weight(1f),
              commonDrawerState = state
            )
            CommonDrawerFooter(
              commonDrawerState = state
            )
          }
        }
      }
    }
  ) {
    content()
  }
}

class CommonDrawerState {
  val customDrawerState = CustomDrawerState()

  suspend fun open() {
    customDrawerState.open()
  }

  suspend fun close() {
    customDrawerState.close()
  }
}