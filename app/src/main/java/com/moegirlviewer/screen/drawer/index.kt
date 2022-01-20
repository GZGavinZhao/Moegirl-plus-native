package com.moegirlviewer.screen.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.painterResource
import com.moegirlviewer.R
import com.moegirlviewer.component.customDrawer.CustomDrawer
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.screen.drawer.component.CommonDrawerBody
import com.moegirlviewer.screen.drawer.component.CommonDrawerFooter
import com.moegirlviewer.screen.drawer.component.CommonDrawerHeader

@ExperimentalMaterialApi
@Composable
fun CommonDrawer(
  ref: Ref<CustomDrawerRef>? = null,
  content: @Composable () -> Unit
) {
  val themeColors = MaterialTheme.colors

  CustomDrawer(
    ref = ref,
    drawerContent = {
      Column(
        modifier = Modifier
          .background(color = themeColors.background),
      ) {
        CommonDrawerHeader(it)
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
              color = Color(1f, 1f, 1f, 0.8f),
              blendMode = BlendMode.Lighten
            )
          )

          Column(
            modifier = Modifier
              .matchParentSize()
          ) {
            CommonDrawerBody(
              modifier = Modifier
                .weight(1f),
              drawerRef = it
            )
            CommonDrawerFooter()
          }
        }
      }
    }
  ) {
    content()
  }
}