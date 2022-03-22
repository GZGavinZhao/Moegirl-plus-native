package com.moegirlviewer.component.styled

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.NospzGothicMoeFamily
import java.util.*

@Composable
fun StyledText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle? = null,
  color: Color = style?.color ?: MaterialTheme.colors.text.primary,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontStyle: FontStyle? = null,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  onTextLayout: (TextLayoutResult) -> Unit = {},
) {
  val themeColors = MaterialTheme.colors
  val useSpecialCharSupportedFont by SettingsStore.common.getValue { this.useSpecialCharSupportedFontInApp }.collectAsState(
    initial = false
  )
  val finalFontFamily = fontFamily ?: if (useSpecialCharSupportedFont) NospzGothicMoeFamily else FontFamily.Default

  Text(
    text = text,
    modifier = modifier,
    color = color,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = finalFontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    onTextLayout = onTextLayout,
    style = style ?: LocalTextStyle.current,
  )
}

// 带有clickableText的功能，同时还支持inlineContent
@Composable
fun StyledText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  style: TextStyle? = null,
  fontStyle: FontStyle? = null,
  color: Color = style?.color ?: MaterialTheme.colors.text.primary,
  fontSize: TextUnit = TextUnit.Unspecified,
  fontWeight: FontWeight? = null,
  fontFamily: FontFamily? = null,
  letterSpacing: TextUnit = TextUnit.Unspecified,
  textDecoration: TextDecoration? = null,
  textAlign: TextAlign? = null,
  lineHeight: TextUnit = TextUnit.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  inlineContent: Map<String, InlineTextContent> = mapOf(),
  onTextLayout: (TextLayoutResult) -> Unit = {},
  onClick: ((Int) -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val finalColor = if (color == Color.Unspecified) themeColors.text.primary else color
  val useSpecialCharSupportedFont by SettingsStore.common.getValue { this.useSpecialCharSupportedFontInApp }.collectAsState(
    initial = false
  )
  val finalFontFamily = fontFamily ?: if (useSpecialCharSupportedFont) NospzGothicMoeFamily else FontFamily.Default

  var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
  val pressIndicator = Modifier.pointerInput(onClick) {
    detectTapGestures { pos ->
      onClick?.invoke(layoutResult!!.getOffsetForPosition(pos))
    }
  }

  Text(
    text = text,
    modifier = modifier.then(if (onClick != null) pressIndicator else Modifier),
    color = finalColor,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = finalFontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    inlineContent = inlineContent,
    onTextLayout = {
      layoutResult = it
      onTextLayout(it)
    },
    style = style ?: LocalTextStyle.current,
  )
}

@Composable
fun AnnotatedString.Builder.clickableText(
  text: String,
  tag: String,
  annotation: String = "",
  style: SpanStyle = SpanStyle()
) {
  val themeColors = MaterialTheme.colors

  withStyle(SpanStyle(
    color = themeColors.secondary,
    textDecoration = TextDecoration.Underline
  ).merge(style)) {
    pushStringAnnotation(tag, annotation)
    append(text)
    pop()
  }
}

@Composable
fun rememberLinkedTextScope() = remember {
  LinkedTextScope()
}

class LinkedTextScope {
  private val handlers = mutableMapOf<String, (annotation: String) -> Unit>()

  @Composable
  fun AnnotatedString.Builder.linkedText(
    text: String,
    annotation: String = "",
    style: SpanStyle = SpanStyle(),
    onClick: (annotation: String) -> Unit,
  ) {
    val themeColors = MaterialTheme.colors

    withStyle(SpanStyle(
      color = themeColors.secondary,
    ).merge(style)) {
      val uuid = UUID.randomUUID().toString()
      handlers[uuid] = onClick
      pushStringAnnotation(uuid, annotation)
      append(text)
      pop()
    }
  }

  fun AnnotatedString.clickAcceptor(offset: Int) {
    for (item in handlers) {
      getStringAnnotations(item.key, offset, offset).firstOrNull()?.let {
        handlers[item.key]?.invoke(it.item)
      }
    }
  }
}