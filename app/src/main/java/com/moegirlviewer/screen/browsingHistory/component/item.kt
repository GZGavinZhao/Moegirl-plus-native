package com.moegirlviewer.screen.browsingHistory.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.moegirlviewer.R
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.ui.theme.background2
import com.moegirlviewer.ui.theme.text
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
        Image(
          modifier = Modifier
            .width(70.dp)
            .height(90.dp),
          painter = rememberImagePainter(record.imgUrl),
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

    Text(
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
      Text(
        text = diffNowDate(record.date),
        color = themeColors.text.secondary,
        fontSize = 13.sp
      )
    }
  }
}