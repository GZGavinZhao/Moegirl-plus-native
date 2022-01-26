package com.moegirlviewer.screen.recentChanges.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.screen.compare.ComparePageRouteArguments
import com.moegirlviewer.screen.contribution.ContributionRouteArguments
import com.moegirlviewer.screen.pageRevisions.PageRevisionsRouteArguments
import com.moegirlviewer.screen.recentChanges.util.EditUserOfChanges
import com.moegirlviewer.screen.recentChanges.util.RawRecentChanges
import com.moegirlviewer.ui.theme.GreenPrimary
import com.moegirlviewer.ui.theme.RedAccent
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.*
import java.time.format.DateTimeFormatter

@Composable
fun RecentChangesItem(
  type: String,   // new | edit | log
  pageName: String,
  comment: String,
  users: List<EditUserOfChanges>,
  newLength: Int,
  oldLength: Int,
  revId: Int,
  oldRevId: Int,
  dateISO: String,
  editDetails: List<RawRecentChanges>,
  pageWatched: Boolean
) {
  val themeColors = MaterialTheme.colors
  var visibleDetails by rememberSaveable { mutableStateOf(false) }

  val totalNumberOfEdit = remember(users) {
    users.fold(0) { result, item -> result + item.total }
  }

  val isSingleEdit = totalNumberOfEdit == 1
  val diffSize = newLength - oldLength
  val hasDetails = editDetails.size > 1
  val totalDiffSize = if (hasDetails)
    editDetails.map { it.newlen - it.oldlen }.reduce { result, item -> result + item } else
    diffSize
  val editSummary = remember(comment) { parseEditSummary(comment) }

  Box(
    modifier = Modifier
      .padding(bottom = 1.dp)
      .clickable { gotoArticlePage(pageName) }
      .background(themeColors.surface)
      .padding(10.dp)
  ) {
    Column() {
      ComposedTitle(
        type = type,
        totalDiffSize = totalDiffSize,
        pageName = pageName,
        pageWatched = pageWatched
      )

      if (!hasDetails) SummaryContent(summary = editSummary)
      if (!isSingleEdit) UsersBar(users = users)
      ComposedFooter(
        isSingleEdit = isSingleEdit,
        visibleDetails = visibleDetails,
        totalNumberOfEdit = totalNumberOfEdit,
        firstUserName = users.first().name,
        dateISO = dateISO,
        onVisibleDetailsChange = { visibleDetails = it }
      )
      AnimatedVisibility(
        visible = hasDetails && visibleDetails,
        enter = expandVertically(
          expandFrom = Alignment.Top
        ),
        exit = shrinkVertically(
          shrinkTowards = Alignment.Top
        )
      ) {
        Column(
          modifier = Modifier
            .padding(start = 2.5.dp)
        ) {
          for (item in editDetails) {
            RecentChangesDetailItem(
              type = item.type,
              comment = item.comment,
              userName = item.user,
              newLength = item.newlen,
              oldLength = item.oldlen,
              revId = item.revid,
              oldRevId = item.old_revid,
              dateISO = item.timestamp,
              pageName = pageName,
              visibleCurrentCompareButton = item.type == "edit" && item.revid != revId,
              visiblePrevCompareButton = item.type == "edit"
            )
          }
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.TopEnd
    ) {
      RightFloatedButtons(
        type = type,
        pageName = pageName,
        revId = revId,
        oldRevId = oldRevId
      )
    }
  }
}

@Composable
private fun ComposedTitle(
  type: String,
  totalDiffSize: Int,
  pageName: String,
  pageWatched: Boolean
) {
  val themeColors = MaterialTheme.colors
  val density = LocalDensity.current
  val titlePrefix = remember { mapOf(
    "new" to "(${Globals.context.getString(R.string.new_)})",
    "edit" to "",
    "log" to "(${Globals.context.getString(R.string.log)})"
  ) }[type]!!
  val titlePrefixColor = remember { mapOf(
    "new" to GreenPrimary,
    "edit" to GreenPrimary,
    "log" to Color(0xff9E9E9E)
  ) }[type]!!
  var textWidth by remember { mutableStateOf(0f) }

  Row(
    modifier = Modifier
      .padding(end = 25.dp)
      .height(25.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (type != "log") {
      Text(
        text = (if (totalDiffSize > 0) "+" else "") + totalDiffSize,
        color = if (totalDiffSize >= 0) GreenPrimary else RedAccent,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Text(
      modifier = Modifier
        .padding(end = 5.dp),
      text = titlePrefix,
      color = titlePrefixColor,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .offset(y = (-1).dp)
    ) {
      Text(
        modifier = Modifier
          .noRippleClickable { gotoArticlePage(pageName) }
          .onGloballyPositioned { textWidth = density.run { it.size.width.toDp().value } }
          .zIndex(1f),
        text = pageName,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      )

      if (pageWatched) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .absoluteOffset(y = 0.dp),
          contentAlignment = Alignment.BottomStart
        ) {
          Spacer(modifier = Modifier
            .height(5.dp)
            .width(textWidth.dp)
            .background(themeColors.secondary)
          )
        }
      }
    }
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
        .padding(top = 5.dp, start = 10.dp, end = 25.dp, bottom = 5.dp),
      fontSize = 14.sp,
      text = buildAnnotatedString {
        if (summary.section != null) {
          withStyle(SpanStyle(
            color = themeColors.text.secondary,
            fontStyle = FontStyle.Italic
          )) {
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
private fun UsersBar(
  users: List<EditUserOfChanges>
) {
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .padding(top = 5.dp, end = 30.dp)
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
  ) {
    users.forEachIndexed { index, item ->
      Row(
        modifier = Modifier
          .noRippleClickable { gotoUserPage(item.name) },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          modifier = Modifier
            .padding(end = 5.dp)
            .size(30.dp)
            .clip(CircleShape),
          painter = rememberImagePainter(Constants.avatarUrl + item.name),
          contentDescription = null
        )
        Text(
          text = "${item.name} (×${item.total})",
          color = themeColors.text.secondary,
          fontSize = 13.sp,
        )
      }

      if (index != users.size - 1) {
        Text(
          text = "、",
          color = themeColors.text.secondary
        )
      }
    }
  }
}

@Composable
private fun ComposedFooter(
  isSingleEdit: Boolean,
  visibleDetails: Boolean,
  totalNumberOfEdit: Int,
  firstUserName: String,
  dateISO: String,
  onVisibleDetailsChange: (Boolean) -> Unit
) {
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .padding(top = 5.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    if (!isSingleEdit) {
      Row(
        modifier = Modifier
          .offset(x = (-1.5).dp, y = 2.dp)
          .noRippleClickable { onVisibleDetailsChange(!visibleDetails) }
      ) {
        Icon(
          modifier = Modifier
            .size(20.dp),
          imageVector = if (visibleDetails) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowRight,
          contentDescription = null,
          tint = themeColors.secondary
        )
        Text(
          text = stringResource(id = R.string.toggleRecentChangeDetail,
            stringResource(id = if (visibleDetails) R.string.collapse else R.string.expand),
            totalNumberOfEdit
          ),
          color = themeColors.secondary,
          fontSize = 14.sp
        )
      }
    } else {
      Row(
        modifier = Modifier
          .noRippleClickable { gotoUserPage(firstUserName) },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape),
          painter = rememberImagePainter(Constants.avatarUrl + firstUserName),
          contentDescription = null,
        )

        Column(
          modifier = Modifier
            .padding(start = 5.dp)
        ) {
          Text(
            text = firstUserName,
            fontSize = 13.sp,
            color = themeColors.text.secondary
          )
          Row() {
            Text(
              modifier = Modifier
                .noRippleClickable { gotoUserPage(firstUserName) },
              text = stringResource(id = R.string.talk),
              fontSize = 11.sp,
              color = themeColors.secondary
            )
            Text(
              text = " | ",
              fontSize = 11.sp,
              color = themeColors.text.tertiary
            )
            Text(
              modifier = Modifier
                .noRippleClickable {
                   Globals.navController.navigate(ContributionRouteArguments(userName = firstUserName))
                },
              text = stringResource(id = R.string.contribution),
              fontSize = 11.sp,
              color = themeColors.secondary
            )
          }
        }
      }
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

@Composable
private fun RightFloatedButtons(
  modifier: Modifier = Modifier,
  type: String,
  pageName: String,
  revId: Int,
  oldRevId: Int,
) {
  val themeColors = MaterialTheme.colors

  Column() {

    if (type == "edit") {
      Icon(
        modifier = Modifier
          .size(25.dp)
          .noRippleClickable {
            Globals.navController.navigate(
              "comparePage", ComparePageRouteArguments(
                fromRevId = oldRevId,
                toRevId = revId,
                pageName = pageName
              )
            )
          },
        imageVector = Icons.Filled.CompareArrows,
        contentDescription = null,
        tint = themeColors.secondary
      )
    }

    Icon(
      modifier = Modifier
        .size(25.dp)
        .noRippleClickable {
           Globals.navController.navigate(PageRevisionsRouteArguments(
             pageName = pageName
           ))
        },
      imageVector = Icons.Filled.History,
      contentDescription = null,
      tint = themeColors.secondary
    )
  }
}