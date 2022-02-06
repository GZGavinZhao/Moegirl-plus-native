package com.moegirlviewer.screen.notification.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.notification.Notification
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.diffNowDate
import com.moegirlviewer.util.gotoUserPage
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.parseMoegirlNormalTimestamp

@Composable
fun NotificationScreenItem(
  notification: Notification,
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors
  val bodyText = if (notification._asterisk.body != "")
    notification._asterisk.body else
    notification._asterisk.compactHeader

  Row(
    modifier = Modifier
      .padding(bottom = 1.dp)
      .clickable { onClick() }
      .background(themeColors.surface)
      .padding(vertical = 5.dp, horizontal = 10.dp)
  ) {
    BadgedBox(
      modifier = Modifier
        .noRippleClickable { gotoUserPage(notification.agent.name) },
      badge = {
        if (notification.read == null) {
          Box(
            modifier = Modifier
              .offset((-5).dp, 5.dp)
          ) {
            Badge()
          }
        }
      }
    ) {
      Image(
        modifier = Modifier
          .size(45.dp)
          .clip(CircleShape)
          .background(themeColors.background2)
        ,
        painter = rememberImagePainter(Constants.avatarUrl + notification.agent.name),
        contentDescription = null
      )
    }

    Column(
      modifier = Modifier
        .padding(start = 5.dp)
        .weight(1f)
    ) {
      StyledText(
        text = notification._asterisk.header.toAnnotatedStringOfNotification(),
        fontSize = 14.sp
      )
      StyledText(
        modifier = Modifier
          .padding(top = 5.dp),
        text = bodyText,
        fontSize = 13.sp,
        color = themeColors.text.secondary
      )
      StyledText(
        modifier = Modifier
          .fillMaxWidth(),
        text = diffNowDate(parseMoegirlNormalTimestamp(notification.timestamp.utciso8601)),
        fontSize = 14.sp,
        textAlign = TextAlign.End,
        color = themeColors.text.secondary
      )
    }
  }
}

private fun String.toAnnotatedStringOfNotification(): AnnotatedString {
  val regex = Regex("""<(b|strong)>(.+?)</(b|strong)>""")
  val normalTexts = this.split(regex)
  val strongTexts = regex
    .findAll(this)
    .map { it.groupValues[2] }
    .toList()

  return buildAnnotatedString {
    normalTexts.forEachIndexed { index, item ->
      append(item)
      if (index < strongTexts.size) {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(strongTexts[index]) }
      }
    }
  }
}