package com.moegirlviewer.component.nativeCommentContent

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.nativeCommentContent.util.*
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.openHttpUrl

@Composable
fun NativeCommentContent(
  commentElements: List<CommentElement>,
  linkedTextStyle: SpanStyle = SpanStyle(),
  prefixContents: List<CommentElement> = emptyList(),
  onAnnotatedTextClick: ((AnnotatedString.Range<String>) -> Unit)? = null
) {
  val idNamespacedPrefixContents = remember(prefixContents) {
    prefixContents.map {
      if (it is CommentInlineContent) CommentInlineContent(
        id = "@@prefixContent-${it.id}",
        content = it.content
      ) else it
    }
  }

  val commentTextList = idNamespacedPrefixContents + commentElements
  val inlineContentMap = remember(commentTextList) {
    commentTextList
      .filterIsInstance<CommentInlineContent>()
      .associate { it.id to it.content }
  }

  val annotatedString = buildAnnotatedString {
    for (item in commentTextList) {
      when(item) {
        is CommentText -> withStyle(item.spanStyle) { append(item.text) }
        is CommentLinkedText -> {
          if (item.type != CommentLinkType.INVALID) {
            pushStringAnnotation(item.type.textAnnoTag, item.target)
            withStyle(linkedTextStyle) { append(item.text) }
            pop()
          } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(item.text) }
          }
        }
        is CommentCustomAnnotatedText -> {
          pushStringAnnotation(item.tag, item.annotation)
          withStyle(item.textStyle ?: linkedTextStyle) { append(item.text) }
          pop()
        }
        is CommentInlineContent -> appendInlineContent(item.id)
      }
    }
  }

  val textOnClickHandler = { index: Int ->
    annotatedString.getStringAnnotations(index, index).firstOrNull()?.let {
      when(it.tag) {
        CommentLinkType.INTERNAL.textAnnoTag -> {
          val pageName = it.item.split("#").first()
          val anchor = it.item.split("#").getOrNull(1)
          Globals.navController.navigate(ArticleRouteArguments(
            pageName = pageName,
            anchor = anchor
          ))
        }
        CommentLinkType.EXTERNAL.textAnnoTag -> {
          openHttpUrl(it.item)
        }
        CommentLinkType.NEW.textAnnoTag -> {
          Globals.commonAlertDialog.showText(Globals.context.getString(R.string.articleMissedHint))
        }
        else -> {
          onAnnotatedTextClick?.invoke(it)
        }
      }
    }
  }

  CompositionLocalProvider(
    LocalTextStyle provides LocalTextStyle.current.copy(
      fontSize = 15.sp
    )
  ) {
    StyledText(
      inlineContent = inlineContentMap,
      text = annotatedString,
      onClick = {
        textOnClickHandler(it)
      }
    )
  }
}