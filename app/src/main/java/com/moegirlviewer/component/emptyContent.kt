package com.moegirlviewer.component

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.Globals

@Composable
private fun defaultEmptyContentHeight() = LocalConfiguration.current.screenHeightDp - 56

@Composable
fun EmptyContent(
  height: Dp = defaultEmptyContentHeight().dp,
  message: String = stringResource(id = R.string.emptyContnet)
) {
  val configuration = LocalConfiguration.current
  val themeColors = MaterialTheme.colors
  val imageWidth = configuration.screenWidthDp * 0.5

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(height),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Image(
      modifier = Modifier
        .width(imageWidth.dp),
      painter = painterResource(R.drawable.empty),
      contentDescription = null,
    )

    Text(
      modifier = Modifier
        .padding(top = 20.dp),
      text = message,
      fontSize = 18.sp,
      color = themeColors.text.secondary
    )
  }
}