package com.moegirlviewer.screen.browsingHistory.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

  Surface() {
    Box(
      modifier = Modifier
        .sideBorder(BorderSide.BOTTOM, 1.dp, themeColors.background2)
        .height(90.dp)
        .background(themeColors.surface)
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick
        ),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .absoluteOffset(
            x = if (record.imgUrl != null) 0.dp else 5.dp,
            y = if (record.imgUrl != null) 0.dp else 5.dp
          )
      ) {
        if (record.imgUrl != null) {
          AsyncImage(
            modifier = Modifier
              .width(70.dp)
              .height(90.dp),
            model = rememberImageRequest(data = record.imgUrl),
            contentDescription = null,
            contentScale = ContentScale.Fit
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

      StyledText(
        modifier = Modifier
          .padding(start = 5.dp)
          .width((14 * 15).dp),
        text = record.pageName,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .absoluteOffset(x = (-5).dp, y = (-5).dp),
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
}