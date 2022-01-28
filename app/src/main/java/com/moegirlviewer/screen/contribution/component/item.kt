package com.moegirlviewer.screen.contribution.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.UserTail
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.compare.ComparePageRouteArguments
import com.moegirlviewer.screen.pageRevisions.PageRevisionsRouteArguments
import com.moegirlviewer.ui.theme.GreenPrimary
import com.moegirlviewer.ui.theme.RedAccent
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.*
import java.time.format.DateTimeFormatter

@Composable
fun ContributionItem(
  pageName: String,
  revId: Int,
  prevRevId: Int,
  dateISO: String,
  summary: String,
  diffSize: Int?,
) {
  val themeColors = MaterialTheme.colors
  val editSummary = remember(summary) { parseEditSummary(summary) }

  Column(
    modifier = Modifier
      .padding(bottom = 1.dp)
      .background(themeColors.surface)
      .clickable {
        Globals.navController.navigate(ArticleRouteArguments(
          pageName = pageName,
          revId = revId
        ))
      }
      .padding(horizontal = 10.dp, vertical = 5.dp)
  ) {
    ComposedTitle(
      diffSize = diffSize,
      pageName = pageName
    )
    SummaryContent(
      summary = editSummary
    )
    ComposedFooter(
      dateISO = dateISO,
      pageName = pageName,
      revId = revId,
      prevRevId = prevRevId
    )
  }
}

@Composable
private fun ComposedTitle(
  diffSize: Int?,
  pageName: String
) {
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .padding(end = 25.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (diffSize != null) {
      Text(
        text = (if (diffSize > 0) "+" else "") + diffSize,
        color = if (diffSize >= 0) GreenPrimary else RedAccent,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    } else {
      Text(
        text = "?",
        color = themeColors.text.secondary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    Text(
      modifier = Modifier
        .padding(start = 5.dp)
        .noRippleClickable { gotoArticlePage(pageName) },
      text = pageName,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun SummaryContent(
  summary: EditSummary
) {
  val themeColors = MaterialTheme.colors

  Box(
    modifier = Modifier
      .fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    Text(
      modifier = Modifier
        .padding(top = 5.dp, start = 10.dp, end = 25.dp),
      fontSize = 14.sp,
      text = buildAnnotatedString {
        if (summary.section != null) {
          withStyle(
            SpanStyle(
            color = themeColors.text.secondary,
            fontStyle = FontStyle.Italic
          )
          ) {
            append("→${summary.section}  ")
          }
        }

        if (summary.body != null) {
          append(summary.body)
        } else {
          withStyle(SpanStyle(color = themeColors.text.secondary)) {
            append(Globals.context.getString(R.string.noSummaryOnCurrentEdit))
          }
        }
      }
    )
  }
}

@Composable
private fun ComposedFooter(
  dateISO: String,
  pageName: String,
  revId: Int,
  prevRevId: Int
) {
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .padding(top = 10.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        modifier = Modifier
          .noRippleClickable {
            Globals.navController.navigate(ComparePageRouteArguments(
              fromRevId = prevRevId,
              toRevId = revId,
              pageName = pageName
            ))
          },
        text = stringResource(id = R.string.diff),
        fontSize = 13.sp,
        color = themeColors.secondary
      )

      Text(
        text = " | ",
        color = themeColors.text.tertiary,
        fontSize = 13.sp,
      )

      Text(
        modifier = Modifier
          .noRippleClickable {
            Globals.navController.navigate(PageRevisionsRouteArguments(
              pageName = pageName
            ))
          },
        text = stringResource(id = R.string.history),
        fontSize = 13.sp,
        color = themeColors.secondary
      )
    }

    Text(
      text = remember(dateISO) {
        parseMoegirlNormalTimestamp(dateISO).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      },
      color = themeColors.text.secondary,
      fontSize = 14.sp
    )
  }
}