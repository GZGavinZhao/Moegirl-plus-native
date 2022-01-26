package com.moegirlviewer.screen.compare.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.UserTail
import com.moegirlviewer.screen.compare.util.DiffLine
import com.moegirlviewer.screen.compare.util.DiffRowContentType
import com.moegirlviewer.screen.compare.util.DiffRowMarker
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.gotoUserPage
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.sideBorder

@Composable
fun CompareScreenDiffContent(
  diffLines: List<DiffLine>,
  userName: String? = null,
  comment: String? = null,
  isCompareText: Boolean = false
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 5.dp)
      .padding(top = 3.dp)
      .verticalScroll(rememberScrollState())
    ) {
      if (!isCompareText) {
        ComposedHeader(
          userName = userName!!,
          comment = comment
        )
      }

      DiffContentBody(diffLines = diffLines)
    }
}

@Composable
private fun ComposedHeader(
  userName: String,
  comment: String?,
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
      Image(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .noRippleClickable { gotoUserPage(userName) },
        painter = rememberImagePainter(Constants.avatarUrl + userName),
        contentDescription = null
      )

      Text(
        modifier = Modifier
          .padding(start = 5.dp)
          .noRippleClickable { gotoUserPage(userName) },
        text = userName,
        fontSize = 14.sp,
        color = themeColors.secondary
      )

      UserTail(userName)
    }

    Box(
      modifier = Modifier
        .padding(10.dp)
    ) {
      Text(
        text = if (comment != null)
          "${stringResource(id = R.string.summary)}：$comment" else
          "（${stringResource(id = R.string.noSummary)}）",
        color = if (comment != null)
          LocalTextStyle.current.color else
          themeColors.text.secondary,
        fontSize = 14.sp
      )
    }
  }
}

@Composable
private fun ColumnScope.DiffContentBody(
  diffLines: List<DiffLine>
) {
  val themeColors = MaterialTheme.colors
  val usingBorderColors = if (themeColors.isLight) borderColors.normal else borderColors.night
  val usingContentColors = if (themeColors.isLight) contentColors.normal else contentColors.night
  
  if (diffLines.isEmpty()) {
    Center(
      modifier = Modifier
        .weight(1f)
    ) {
      Text(
        text = stringResource(id = R.string.noDiff),
        fontSize = 18.sp,
        color = themeColors.text.tertiary
      )
    }
  }
  Column() {
    diffLines.forEachIndexed { _, line ->
      Column() {
        Text(
          text = line.lineHint
        )
        Spacer(modifier = Modifier
          .padding(end = 10.dp, bottom = 10.dp)
          .height(3.dp)
          .fillMaxWidth()
          .background(themeColors.secondary)
        )

        line.rows.forEachIndexed { _, row ->
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .padding(end = 5.dp)
            ) {
              if (row.marker != DiffRowMarker.NONE) {
                Icon(
                  modifier = Modifier
                    .padding(bottom = 3.dp)
                    .size(20.dp),
                  imageVector = if (row.marker == DiffRowMarker.PLUS)
                    Icons.Filled.Add else
                    Icons.Filled.Remove,
                  tint = themeColors.text.secondary,
                  contentDescription = null
                )
              } else {
                Spacer(modifier = Modifier.width(20.dp))
              }
            }

            Box(
              modifier = Modifier
                .padding(bottom = 5.dp)
                .sideBorder(BorderSide.LEFT, 5.dp, usingBorderColors[row.marker]!!)
                .padding(start = 5.dp, top = 3.dp, bottom = 3.dp)
            ) {
              Text(
                fontSize = 14.sp,
                text = buildAnnotatedString {
                  for (content in row.content) {
                    withStyle(SpanStyle(background = usingContentColors[content.type]!!)) {
                      append(content.text)
                    }
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

val borderColors = DiffContentColors(
  normal = mapOf(
    DiffRowMarker.NONE to Color(0xffe6e6e6),
    DiffRowMarker.PLUS to Color(0xffd8ecff),
    DiffRowMarker.MINUS to Color(0xffffe49c)
  ),
  night = mapOf(
    DiffRowMarker.NONE to Color(0xff6D6D6D),
    DiffRowMarker.PLUS to Color(0xff81DAF5),
    DiffRowMarker.MINUS to Color(0xffFFCC00)
  )
)

val contentColors = DiffContentColors(
  normal = mapOf(
    DiffRowContentType.PLAIN to Color.Transparent,
    DiffRowContentType.ADD to Color(0xffd8ecff),
    DiffRowContentType.DELETE to Color(0xffffe49c)
  ),
  night = mapOf(
    DiffRowContentType.PLAIN to Color.Transparent,
    DiffRowContentType.ADD to Color(0xff81DAF5).copy(alpha = 0.3f),
    DiffRowContentType.DELETE to Color(0xffFFCC00).copy(alpha = 0.3f)
  )
)

class DiffContentColors<Key : Enum<*>>(
  val normal: Map<Key, Color>,
  val night: Map<Key, Color>
)