package com.moegirlviewer.screen.searchResult.component

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.api.search.bean.SearchResultBean
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.sideBorder
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

typealias SearchResultItem = SearchResultBean.Query.Search

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SearchResultItem(
  data: SearchResultItem,
  keyword: String,
  onClick: (pageName: String) -> Unit
) {
  val context = LocalContext.current
  val themeColors = MaterialTheme.colors

  val subInfoText = remember(data) {
    when {
      data.redirecttitle != null ->
        context.getString(R.string.searchResultRedirectTitle, data.redirecttitle)
      data.sectiontitle != null ->
        context.getString(R.string.searchResultSectionTitle, keyword)
      data.categorysnippet != null ->
        "${context.getString(R.string.searchResultFromPageCategories)}：${data.categorysnippet}"
      else -> ""
    }
  }

//  AnimatedVisibility(
//    visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
//    enter = scaleIn() + fadeIn(),
//    exit = scaleOut() + fadeOut()
//  ) {
    Card(
      modifier = Modifier
        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
        .fillMaxWidth(),
      backgroundColor = themeColors.surface,
      elevation = 1.dp,
      onClick = { onClick(data.title) }
    ) {
      Column(
        modifier = Modifier
          .padding(5.dp)
      ) {
        ComposedHeader(
          title = data.title,
          subInfoText = subInfoText
        )

        Box(
          modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth()
            .sideBorder(BorderSide.TOP, 2.dp, themeColors.secondary)
            .sideBorder(BorderSide.BOTTOM, 2.dp, themeColors.secondary)
            .padding(vertical = 5.dp)
        ) {
          SearchContent(
            html = data.snippet
          )
        }

        ComposedFooter(
          date = LocalDate.parse(data.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        )
      }
    }
//  }
}

@Composable
private fun ComposedHeader(
  title: String,
  subInfoText: String
) {
  val themeColors = MaterialTheme.colors

  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    StyledText(
      text = title,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )

    StyledText(
      modifier = Modifier
        .padding(start = 5.dp, end = 3.dp)
        .weight(1f),
      text = subInfoText,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Right,
      style = TextStyle(
        color = themeColors.primary,
        fontStyle = FontStyle.Italic
      )
    )
  }
}

@Composable
private fun SearchContent(
  html: String
) {
  val themeColors = MaterialTheme.colors

  if (html.trim() != "") {
    val htmlDoc = Jsoup.parse(html)
    val elements = htmlDoc.body().childNodes()

    StyledText(
      buildAnnotatedString {
        elements.forEach {
          withStyle(
            style = SpanStyle(
              fontSize = 15.sp
            )
          ) {
            if (it is Element && it.hasClass("searchmatch")) {
              withStyle(
                style = SpanStyle(
                  background = themeColors.secondaryVariant
                )
              ) {
                append(it.text())
              }
            } else {
              append((it as TextNode).text())
            }
          }
        }
      }
    )
  }
}

@Composable
private fun ComposedFooter(
  date: LocalDate
) {
  val themeColors = MaterialTheme.colors
  val formatter = DateTimeFormatter.ofPattern(
    stringResource(id = R.string.pageLastUpdateDate)
  )

  Box(
    modifier = Modifier
      .fillMaxWidth(),
    contentAlignment = Alignment.CenterEnd
  ) {
    StyledText(
      color = themeColors.text.secondary,
      text = date.format(formatter),
      fontSize = 15.sp
    )
  }
}