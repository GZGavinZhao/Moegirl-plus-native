package com.moegirlviewer.screen.contribution.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.compare.ComparePageRouteArguments
import com.moegirlviewer.screen.pageRevisions.PageRevisionsRouteArguments
import com.moegirlviewer.theme.GreenPrimary
import com.moegirlviewer.theme.RedAccent
import com.moegirlviewer.theme.text
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
          pageKey = PageNameKey(pageName),
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
      StyledText(
        text = (if (diffSize > 0) "+" else "") + diffSize,
        color = if (diffSize >= 0) themeColors.primaryVariant else RedAccent,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    } else {
      StyledText(
        text = "?",
        color = themeColors.text.secondary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    StyledText(
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
    StyledText(
      modifier = Modifier
        .padding(top = 10.dp, start = 10.dp, end = 25.dp),
      fontSize = 14.sp,
      text = buildAnnotatedString {
        if (summary.section != null) {
          withStyle(
            SpanStyle(
            color = themeColors.text.secondary,
            textGeometricTransform = remember { TextGeometricTransform.Italic() }
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
      .padding(top = 15.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      StyledText(
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
        color = themeColors.primaryVariant
      )

      StyledText(
        text = " | ",
        color = themeColors.text.tertiary,
        fontSize = 13.sp,
      )

      StyledText(
        modifier = Modifier
          .noRippleClickable {
            Globals.navController.navigate(PageRevisionsRouteArguments(
              pageName = pageName
            ))
          },
        text = stringResource(id = R.string.history),
        fontSize = 13.sp,
        color = themeColors.primaryVariant
      )
    }

    StyledText(
      text = remember(dateISO) {
        parseMoegirlNormalTimestamp(dateISO).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      },
      color = themeColors.text.secondary,
      fontSize = 14.sp
    )
  }
}