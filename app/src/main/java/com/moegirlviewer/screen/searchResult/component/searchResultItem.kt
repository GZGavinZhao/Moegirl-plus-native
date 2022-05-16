package com.moegirlviewer.screen.searchResult.component

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.api.search.bean.SearchResultBean
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.Italic
import com.moegirlviewer.util.sideBorder
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.OptIn
import kotlin.String
import kotlin.Unit
import kotlin.math.min

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SearchResultItem(
  data: SearchResultItemData,
  keyword: String,
  onClick: (pageName: String) -> Unit
) {
  val context = LocalContext.current
  val themeColors = MaterialTheme.colors

//  val subInfoText = remember(data) {
//    when {
//      data.redirecttitle != null ->
//        context.getString(R.string.searchResultRedirectTitle, data.redirecttitle)
//      data.sectiontitle != null ->
//        context.getString(R.string.searchResultSectionTitle, keyword)
//      data.categorysnippet != null ->
//        "${context.getString(R.string.searchResultFromPageCategories)}：${data.categorysnippet}"
//      else -> null
//    }
//  }

  Box(
    modifier = Modifier
      .padding(bottom = 15.dp, start = 10.dp, end = 10.dp)
      .fillMaxWidth()
      .height(150.dp)
      .clip(RoundedCornerShape(5.dp))
      .background(themeColors.surface)
      .clickable { onClick(data.title) },
  ) {
    Box(
      modifier = Modifier
        .offset(0.dp, 0.dp)
        .width(100.dp)
        .fillMaxHeight(),
      contentAlignment = Alignment.Center
    ) {
      if (data.imageUrl != null) {
        var imageLoadFailed by remember { mutableStateOf(false) }
        AsyncImage(
          modifier = Modifier
            .fillMaxSize(),
          model = rememberImageRequest(data.imageUrl.source),
          contentDescription = null,
          contentScale = if (imageLoadFailed) ContentScale.Fit else ContentScale.Crop,
          alignment = if (imageLoadFailed) Alignment.Center else Alignment.TopCenter,
          onError = { imageLoadFailed = true }
        )
      } else {
        Center(
          modifier = Modifier
            .fillMaxSize()
        ) {
          StyledText(
            text = stringResource(id = R.string.noImage),
            color = themeColors.text.secondary,
          )
        }
      }
    }

    Column(
      modifier = Modifier
        .padding(start = 100.dp)
        .fillMaxWidth()
        .padding(vertical = 5.dp, horizontal = 10.dp)
    ) {
      StyledText(
        text = data.title,
        fontSize = 18.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold
      )

//      if (subInfoText != null) {
//        StyledText(
//          modifier = Modifier
//            .padding(top = 3.dp),
//          text = subInfoText,
//          maxLines = 1,
//          overflow = TextOverflow.Ellipsis,
//          textAlign = TextAlign.Right,
//          color = themeColors.primaryVariant,
//          style = TextStyle(
//            textGeometricTransform = remember { TextGeometricTransform.Italic() }
//          )
//        )
//      }

      SearchContent(html = data.snippet)
      ComposedFooter(
        date = LocalDate.parse(data.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
      )
    }
  }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ColumnScope.SearchContent(
  html: String,
) {
  val themeColors = MaterialTheme.colors

  if (html.trim() != "") {
    val htmlDoc = remember(html) { Jsoup.parse(html) }
    val elements = htmlDoc.body().childNodes()

    Box(
      modifier = Modifier
        .padding(top = 5.dp)
        .weight(1f),
      contentAlignment = Alignment.BottomCenter
    ) {
      Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
      ) {
        StyledText(
          modifier = Modifier
            .fillMaxSize(),
          maxLines = 5,
          overflow = TextOverflow.Ellipsis,
          text = buildAnnotatedString {
            elements.forEach {
              withStyle(
                style = SpanStyle(
                  fontSize = 13.sp,
                  color = themeColors.text.secondary
                )
              ) {
                if (it is Element && it.hasClass("searchmatch")) {
                  withStyle(
                    style = SpanStyle(
                      color = themeColors.primaryVariant,
                      fontWeight = FontWeight.Bold
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

//      Spacer(modifier = Modifier
//        .fillMaxWidth()
//        .height(30.dp)
//        .background(
//          Brush.verticalGradient(listOf(
//            Color.Transparent,
//            themeColors.surface
//          ))
//        )
//      )
    }
  } else {
    StyledText(
      text = stringResource(id = R.string.pageNoContent),
      color = themeColors.text.secondary
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

  Row(
    modifier = Modifier
      .padding(top = 5.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    StyledText(
      color = themeColors.text.secondary,
      text = date.format(formatter),
      fontSize = 13.sp
    )
  }
}

class SearchResultItemData(
  searchItem: SearchResultBean.Query.Search,
  val imageUrl: SearchResultBean.Query.MapValue.Thumbnail? = null
) : SearchResultBean.Query.Search(
   categorysnippet = searchItem.categorysnippet,
   redirecttitle = searchItem.redirecttitle,
   sectiontitle = searchItem.sectiontitle,
   ns = searchItem.ns,
   pageid = searchItem.pageid,
   snippet = searchItem.snippet,
   timestamp = searchItem.timestamp,
   title = searchItem.title,
)