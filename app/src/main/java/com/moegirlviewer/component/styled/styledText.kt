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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.NospzGothicMoeFamily

@Composable
fun StyledText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colors.text.primary,
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
  style: TextStyle = LocalTextStyle.current,
) {
  val themeColors = MaterialTheme.colors
  val finalFontFamily = fontFamily ?: NospzGothicMoeFamily

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
    style = style,
  )
}

// 带有clickableText的功能，同时还支持inlineContent
@Composable
fun StyledText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  color: Color = Color.Unspecified,
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
  inlineContent: Map<String, InlineTextContent> = mapOf(),
  onTextLayout: (TextLayoutResult) -> Unit = {},
  style: TextStyle = LocalTextStyle.current,
  onClick: ((Int) -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val finalColor = if (color == Color.Unspecified) themeColors.text.primary else color
  val finalFontFamily = fontFamily ?: NospzGothicMoeFamily

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
    style = style,
  )
}