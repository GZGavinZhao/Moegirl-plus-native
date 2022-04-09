package com.moegirlviewer.screen.home.component.newPagesCard.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.moegirlviewer.R
import com.moegirlviewer.api.editingRecord.bean.NewPagesBean
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.gotoArticlePage

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ListLayoutNewPages(
  pageList: List<NewPagesBean.Query.MapValue>
) {
  val pagerState = rememberFromMemory("pagerState") { PagerState(0) }
  val chunkedPageList = remember(pageList) { pageList.chunked(3) }

  HorizontalPager(
    modifier = Modifier
      .height(235.dp),
    state = pagerState,
    count = chunkedPageList.size,
    itemSpacing = (-20).dp
  ) { currentPage ->
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .padding(top = 5.dp, bottom = 5.dp, end = 10.dp)
    ) {
      for (item in chunkedPageList[currentPage]) {
        Item(
          title = item.title,
          introduction = if (item.extract == "") null else item.extract,
          imageUrl = item.thumbnail?.source,
          onClick = { gotoArticlePage(item.title) }
        )
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
        .padding(horizontal = 10.dp, vertical = 7.5.dp)
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