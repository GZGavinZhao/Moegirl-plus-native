package com.moegirlviewer.screen.home.component.newPagesCard.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.api.editingRecord.bean.NewPagesBean
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.gotoArticlePage

@Composable
fun ColumnLayoutNewPages(
  pageList: List<NewPagesBean.Query.MapValue>
) {
  val scrollState = rememberFromMemory("scrollState") { ScrollState(0) }

  Row(
    modifier = Modifier
      .padding(10.dp)
      .horizontalScroll(scrollState)
  ) {
    for ((index, item) in pageList.withIndex()) {
      Item(
        title = item.title,
        imageUrl = item.thumbnail?.source,
        isFirstItem = index == 0,
        onClick = { gotoArticlePage(item.title) }
      )
    }
  }
}

@Composable
private fun Item(
  title: String,
  imageUrl: String?,
  isFirstItem: Boolean,
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors

  Column(
    modifier = Modifier
      .padding(start = if (isFirstItem) 0.dp else 10.dp)
      .clickable { onClick() },
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (imageUrl != null) {
      AsyncImage(
        modifier = Modifier
          .width(120.dp)
          .height(160.dp),
        model = rememberImageRequest(imageUrl),
        placeholder = painterResource(id = R.drawable.placeholder),
        contentDescription = null,
        alignment = Alignment.TopCenter,
        contentScale = ContentScale.Crop
      )
    } else {
      Box(
        modifier = Modifier
          .width(120.dp)
          .height(160.dp)
          .background(themeColors.background2),
        contentAlignment = Alignment.Center
      ) {
        StyledText(
          text = stringResource(id = R.string.noImage),
          color = themeColors.text.secondary
        )
      }
    }


    StyledText(
      modifier = Modifier
        .padding(top = 5.dp)
        .width(120.dp),
      text = title,
      fontSize = 13.sp,
      color = themeColors.secondary,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}