package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.EmptyContent
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.rememberLinkedTextScope
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.screen.home.util.RecommendationPagesResult
import com.moegirlviewer.screen.home.util.getRecommendationPages
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.printRequestErr
import kotlinx.coroutines.launch

@Composable
fun RecommendationCard(
  state: RecommendationCardState
) {
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  HomeCardContainer(
    icon = Icons.Filled.Stars,
    title = stringResource(id = R.string.recommendation),
    minHeight = 100.dp,
    loadStatus = state.status,
    moreLink = if (state.recommendationPages != null) MoreLink(
      title = stringResource(id = R.string.viewMore),
      onClick = { gotoArticlePage("Category:" + state.recommendationPages!!.sourceCategoryName) }
    ) else null,
    onReload = {
      scope.launch { state.reload() }
    }
  ) {
    if (state.status == LoadStatus.SUCCESS && state.recommendationPages == null) {
      EmptyContent(
        height = 300.dp,
        message = stringResource(id = R.string.noRecommendationPageHint)
      )
    }

    if (state.recommendationPages != null) {
      Column() {
        val linkedTextScope = rememberLinkedTextScope()
        val annotatedString = buildAnnotatedString {
          linkedTextScope.run {
            append(stringResource(id = R.string.becauseYouRead) + "：")
            linkedText(
              text = state.recommendationPages!!.sourcePageName,
              onClick = { gotoArticlePage(state.recommendationPages!!.sourcePageName) }
            )
            append("（")
            linkedText(
              text = stringResource(id = R.string.ofCategory) + "：" + state.recommendationPages!!.sourceCategoryName,
              onClick = { gotoArticlePage("Category:" + state.recommendationPages!!.sourceCategoryName) }
            )
            append("）")
          }
        }

        StyledText(
          modifier = Modifier
            .padding(10.dp),
          text = annotatedString,
          color = themeColors.text.secondary,
          onClick = {
            linkedTextScope.run { annotatedString.clickAcceptor(it) }
          }
        )

        for (item in state.recommendationPages!!.body) {
          Item(
            title = item.pageName,
            imageUrl = item.imageUrl,
            introduction = if (item.introduction != "") item.introduction else null,
            onClick = { gotoArticlePage(item.pageName) }
          )
        }
      }
    }
  }
}

@Composable
private fun Item(
  title: String,
  introduction: String? = null,
  imageUrl: String? = null,
  onClick: () -> Unit,
) {
  val themeColors = MaterialTheme.colors

  RippleColorScope(color = themeColors.primaryVariant) {
    Row(
      modifier = Modifier
        .clickable { onClick() }
        .padding(horizontal = 10.dp, vertical = 5.dp)
        .height(60.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
        modifier = Modifier
          .size(60.dp)
          .clip(RoundedCornerShape(10.dp)),
        model = rememberImageRequest(imageUrl ?: R.drawable.moemoji),
        placeholder = painterResource(id = R.drawable.placeholder),
        contentDescription = null,
        contentScale = if (imageUrl != null) ContentScale.Crop else ContentScale.Inside,
        alignment = if (imageUrl != null) Alignment.TopCenter else Alignment.Center
      )

      Column(
        modifier = Modifier
          .padding(start = 10.dp)
          .weight(1f)
          .height(60.dp),
        verticalArrangement = Arrangement.SpaceEvenly
      ) {
        StyledText(
          text = title,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        StyledText(
          text = introduction ?: stringResource(id = R.string.noIntroduction),
          fontSize = 14.sp,
          color = if (introduction != null) themeColors.text.secondary else themeColors.text.tertiary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

class RecommendationCardState : HomeScreenCardState() {
  var recommendationPages by mutableStateOf<RecommendationPagesResult?>(null)
  var status by mutableStateOf(LoadStatus.INITIAL)

  override suspend fun reload() {
    status = LoadStatus.LOADING
    try {
      recommendationPages = getRecommendationPages(5)
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      status = LoadStatus.FAIL
      printRequestErr(e, "加载推荐卡片数据失败")
    }
  }
}