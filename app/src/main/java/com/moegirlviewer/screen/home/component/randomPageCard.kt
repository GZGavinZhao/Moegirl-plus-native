package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.home.HomeScreenModel
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.gotoArticlePage
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@Composable
fun RandomPageCard() {
  val model: HomeScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val data = model.randomPageData.body

  CardContainer(
    icon = ImageVector.vectorResource(R.drawable.dice_5),
    title = stringResource(id = R.string.randomArticle),
    moreLink = remember { MoreLink(
      title = Globals.context.getString(R.string.moreRandomArticle),
      onClick = {
        Globals.navController.navigate("randomPages")
      }
    ) },
    loadStatus = model.randomPageData.status,
    onClick = {
      if (model.randomPageData.status == LoadStatus.SUCCESS) gotoArticlePage(data!!.title)
    },
    onReload = {
      scope.launch { model.loadRandomPageData() }
    }
  ) {
    if (data != null) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        if (data.thumbnail != null) {
          val imagePainter = rememberImagePainter(data.thumbnail.source) {
            crossfade(true)
            this.placeholder(R.drawable.placeholder)
            this.error(R.drawable.placeholder)
          }

          Image(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp),
            painter = imagePainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = if (imagePainter.state is ImagePainter.State.Success)
              Alignment.TopCenter else
              Alignment.Center
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp)
              .background(themeColors.background2),
            contentAlignment = Alignment.Center
          ) {
            StyledText(
              text = stringResource(id = R.string.noImage),
              fontSize = 20.sp,
              color = themeColors.text.tertiary
            )
          }
        }

        Column(
          modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
        ) {
          StyledText(
            text = data.title,
            fontSize = 20.sp
          )

          StyledText(
            modifier = Modifier
              .padding(top = 10.dp),
            text = if (data.extract != "") data.extract else stringResource(id = R.string.noIntroduction),
            fontSize = 15.sp,
            color = if (data.extract != "") themeColors.text.primary else themeColors.text.tertiary
          )
        }
      }
    } else {
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(350.dp)
      )
    }
  }
}