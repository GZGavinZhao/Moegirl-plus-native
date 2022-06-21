package com.moegirlviewer.screen.compare.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.UserAvatar
import com.moegirlviewer.component.UserTail
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.recentChanges.component.MultiRevisionHint
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.gotoUserPage
import com.moegirlviewer.util.noRippleClickable

@Composable
fun DiffInfo(
  userName: String,
  comment: String?,
  multiRevisionHint: MultiRevisionHint? = null
) {
  val themeColors = MaterialTheme.colors

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      UserAvatar(
        modifier = Modifier
          .size(30.dp),
        userName = userName,
      )

      StyledText(
        modifier = Modifier
          .padding(start = 5.dp)
          .noRippleClickable { gotoUserPage(userName) },
        text = userName,
        fontSize = 16.sp,
        color = themeColors.primaryVariant
      )

      UserTail(userName)
    }

    Column(
      modifier = Modifier
        .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 15.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      StyledText(
        text = if (comment != null && comment != "")
          "${stringResource(id = R.string.summary)}：$comment" else
          "（${stringResource(id = R.string.noSummary)}）",
        color = if (comment != null && comment != "")
          LocalTextStyle.current.color else
          themeColors.text.secondary,
        fontSize = 14.sp
      )

      if (multiRevisionHint != null) {
        val editorTotalText = if (multiRevisionHint.editorTotal == 1)
          stringResource(id = R.string.multiRevisionHint_same) else
          stringResource(id = R.string.multiRevisionHint_number, multiRevisionHint.editorTotal)

        StyledText(
          modifier = Modifier
            .padding(top = 5.dp),
          text = "（${stringResource(id = R.string.multiRevisionHint, editorTotalText, multiRevisionHint.revisionTotal)}）",
          color = themeColors.text.secondary,
          fontSize = 14.sp
        )
      }
    }
  }
}