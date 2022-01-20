package com.moegirlviewer.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.noRippleClickable

@Composable
fun UserTail(
  userName: String,
  fontSize: TextUnit = 13.sp
) {
  val themeColors = MaterialTheme.colors
  val currentTextStyle = LocalTextStyle.current
  val textStyle = remember { currentTextStyle.copy(fontSize = fontSize) }


  Row() {
    CompositionLocalProvider(
      LocalTextStyle provides textStyle
    ) {
      Text(
        text = " (",
        color = themeColors.text.secondary
      )
      Text(
        modifier = Modifier
          .noRippleClickable { gotoArticlePage("User_talk:$userName") },
        text = stringResource(id = R.string.talk),
        color = themeColors.secondary
      )
      Text(
        text = " | ",
        color = themeColors.text.tertiary
      )
      Text(
        modifier = Modifier
          .noRippleClickable { /* 前往贡献 */ },
        text = stringResource(id = R.string.contribution),
        color = themeColors.secondary
      )
      Text(
        text = ")",
        color = themeColors.text.secondary
      )
    }
  }
}