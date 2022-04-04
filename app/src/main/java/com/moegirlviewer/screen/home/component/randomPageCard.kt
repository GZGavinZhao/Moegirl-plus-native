package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.page.bean.RandomPageResBean
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.printRequestErr
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@Composable
fun RandomPageCard(
  state: RandomPageCardState
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  RippleColorScope(color = themeColors.primaryVariant) {
    HomeCardContainer(
      icon = ImageVector.vectorResource(R.drawable.dice_5),
      title = stringResource(id = R.string.randomArticle),
      moreLink = remember { MoreLink(
        title = Globals.context.getString(R.string.moreRandomArticle),
        onClick = {
          Globals.navController.navigate("randomPages")
        }
      ) },
      loadStatus = state.status,
      onClick = {
        if (state.status == LoadStatus.SUCCESS) gotoArticlePage(state.pageData!!.title)
      },
      onReload = {
        scope.launch { state.reload() }
      }
    ) {
      if (state.pageData != null) {
        var imageLoaded by rememberSaveable { mutableStateOf(false) }
        Column(
          modifier = Modifier
            .fillMaxWidth()
        ) {
          if (state.pageData!!.thumbnail != null) {
            AsyncImage(
              modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
              model = rememberImageRequest(state.pageData!!.thumbnail!!.source),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              placeholder = painterResource(id = R.drawable.placeholder),
              alignment = if (imageLoaded) Alignment.TopCenter else Alignment.Center,
              onLoading = { imageLoaded = false },
              onSuccess = { imageLoaded = true }
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
              text = state.pageData!!.title,
              fontSize = 20.sp
            )

            StyledText(
              modifier = Modifier
                .padding(top = 10.dp),
              text = if (state.pageData!!.extract != "") state.pageData!!.extract else stringResource(id = R.string.noIntroduction),
              fontSize = 15.sp,
              lineHeight = 25.sp,
              color = if (state.pageData!!.extract != "") themeColors.text.primary else themeColors.text.tertiary,
              maxLines = 5,
              overflow = TextOverflow.Ellipsis
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
}

class RandomPageCardState : HomeScreenCardState() {
  var pageData by mutableStateOf<RandomPageResBean.Query.MapValue?>(null)
  var status by mutableStateOf(LoadStatus.INITIAL)

  override suspend fun reload() {
    status = LoadStatus.LOADING
    try {
      val res = PageApi.getRandomPage(10)
      // 尽量使用既有图片又有介绍的，如果没有就找有图片的，如果全不符合条件则随机使用一个
      pageData = res.query.pages.values
        .filter { it.thumbnail != null && it.extract != "" }
        .randomOrNull() ?:
          res.query.pages.values
            .filter { it.thumbnail != null }
            .randomOrNull() ?:
              res.query.pages.values.random()
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      status = LoadStatus.FAIL
      printRequestErr(e, "加载随机页面卡片数据失败")
    }
  }
}