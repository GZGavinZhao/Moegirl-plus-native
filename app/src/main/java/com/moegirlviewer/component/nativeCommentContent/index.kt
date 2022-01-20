package com.moegirlviewer.component.nativeCommentContent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.component.nativeCommentContent.util.CommentElement
import com.moegirlviewer.component.nativeCommentContent.util.CommentInlineContent
import com.moegirlviewer.component.nativeCommentContent.util.CommentText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

@Composable
fun NativeCommentContent(
  modifier: Modifier = Modifier,
  commentElements: List<CommentElement>,
//  html: String,
//  maxImageWidth: Int,
  prefixContents: List<CommentElement> = emptyList()
) {
//  val scope = rememberCoroutineScope()
//  val density = LocalDensity.current
//  var htmlParsingResult by rememberFromMemory("htmlParsingResult") { mutableStateOf<List<CommentElement>?>(null) }
//
//  if (htmlParsingResult == null) {
//    LaunchedEffect(true) {
//      withContext(Dispatchers.Default) {
//        htmlParsingResult = parseHtml(html, density, maxImageWidth)
//      }
//    }
//  }

  val idNamespacedPrefixContents = remember(prefixContents) {
    prefixContents.map {
      if (it is CommentInlineContent) CommentInlineContent(
        id = "@@prefixContent-${it.id}",
        content = it.content
      ) else it
    }
  }

  val commentTextList = idNamespacedPrefixContents + commentElements
  val inlineContentMap = commentTextList
    .filterIsInstance<CommentInlineContent>()
    .associate { it.id to it.content }

  CompositionLocalProvider(
    LocalTextStyle provides LocalTextStyle.current.copy(
      fontSize = 15.sp
    )
  ) {
    Text(
      modifier = modifier,
      inlineContent = inlineContentMap,
      text = buildAnnotatedString {
        for (item in commentTextList) {
          when(item) {
            is CommentText -> withStyle(item.spanStyle) { append(item.text) }
            is CommentInlineContent -> appendInlineContent(item.id)
          }
        }
      }
    )
  }
}