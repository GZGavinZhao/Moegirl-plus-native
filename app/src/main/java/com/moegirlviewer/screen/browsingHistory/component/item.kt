package com.moegirlviewer.screen.browsingHistory.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.diffNowDate
import com.moegirlviewer.util.sideBorder

@ExperimentalFoundationApi
@Composable
fun BrowsingHistoryScreenItem(
  record: BrowsingRecord,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
) {
  val themeColors = MaterialTheme.colors

  Box(
    modifier = Modifier
      .padding(top = 10.dp, start = 10.dp, end = 10.dp)
      .height(110.dp)
      .clip(RoundedCornerShape(5.dp))
      .background(themeColors.surface)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .width(70.dp)
          .height(100.dp),
        contentAlignment = Alignment.Center
      ) {
        if (record.imgUrl != null) {
          AsyncImage(
            modifier = Modifier
              .width(70.dp)
              .height(100.dp)
              .clip(RoundedCornerShape(5.dp)),
            model = rememberImageRequest(data = record.imgUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
          )
        } else {
          Image(
            modifier = Modifier
              .width(60.dp)
              .height(80.dp),
            painter = painterResource(R.drawable.moemoji),
            contentDescription = null
          )
        }
      }

      Box(
        modifier = Modifier
          .padding(bottom = 5.dp)
          .fillMaxSize()
          .weight(1f)
          .padding(10.dp),
        contentAlignment = Alignment.Center
      ) {
        StyledText(
          text = record.pageName,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.BottomEnd,
    ) {
      StyledText(
        text = diffNowDate(record.date),
        color = themeColors.text.secondary,
        fontSize = 13.sp
      )
    }
  }
}