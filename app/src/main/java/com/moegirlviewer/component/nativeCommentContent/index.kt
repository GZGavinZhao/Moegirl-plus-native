package com.moegirlviewer.component.nativeCommentContent

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.nativeCommentContent.util.*
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.openHttpUrl

@Composable
fun NativeCommentContent(
  modifier: Modifier = Modifier,
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

  var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
  val pressIndicator = Modifier.pointerInput(textOnClickHandler) {
    detectTapGestures { pos ->
      textOnClickHandler(layoutResult!!.getOffsetForPosition(pos))
    }
  }


  CompositionLocalProvider(
    LocalTextStyle provides LocalTextStyle.current.copy(
      fontSize = 15.sp
    )
  ) {
    Text(
      modifier = modifier.then(pressIndicator),
      inlineContent = inlineContentMap,
      text = annotatedString,
      onTextLayout = { layoutResult = it }
    )
  }
}