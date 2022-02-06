package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

internal val linearParsingMarkupList = listOf(
  PairWikitextMarkup(
    startText = "[[",
    endText = "]]",
    style = SpanStyle(
      color = Color(0xff21A3F1),
    ),
  ),
  PairWikitextMarkup(
    startText = "{{",
    endText = "}}",
    style = SpanStyle(
      color = Color(0xffE38A2E),
    ),
  ),
  PairWikitextMarkup(
    startText = "[http",
    endText = "]",
    style = SpanStyle(
      color = Color(0xff38AD6C)
    ),
  ),
  PairWikitextMarkup(
    startText = "<!--",
    endText = "-->",
    style = SpanStyle(
      color = Color(0xffBEF781)
    )
  ),
  EqualWikitextMarkup(
    text = "'''",
    style = SpanStyle(
      fontWeight = FontWeight.Black,
    ),
  ),
  EqualWikitextMarkup(
    text = "''",
    style = SpanStyle(
      fontStyle = FontStyle.Italic
    )
  )
)

internal val matchParsingMarkupList = listOf(
  InlineEqualWikitextMarkup(
    text = "==",
    style = SpanStyle(
      color = Color.Green
    )
  ),
  InlineEqualWikitextMarkup(
    text = "*",
    style = SpanStyle(
      color = Color.Red
    )
  ),
  InlineEqualWikitextMarkup(
    text = "#",
    style = SpanStyle(
      color = Color.Blue
    )
  ),
)

fun tintWikitext(wikitext: String): AnnotatedString {
  val linearParseResult = linearParseWikitext(wikitext)
  val matchParseResult = matchParseWikitext(wikitext)
  return buildAnnotatedString {
//    for (item in linearParseResult.mergeInlineParseResult(matchParseResult)) {
    for (item in linearParseResult) {
      tintTextByMarkup(item)
    }
  }
}

abstract class WikitextMarkup(
  val style: SpanStyle,
  val contentStyle: SpanStyle = style
)

//class TagWikitextMarkup(
//  val tagName: String,
//  style: SpanStyle,
//  contentStyle: SpanStyle = style
//) : WikitextMarkup(style, contentStyle)

interface TintableWikitextMarkup {
  val startText: String
  val endText: String
  val style: SpanStyle
  val contentStyle: SpanStyle
}

data class ParseResult<T : TintableWikitextMarkup>(
  val content: String = "",
  val contentRange: ClosedRange<Int>,
  val markup: T? = null,
  val containStartMarkup: Boolean = false,
  val containEndMarkup: Boolean = false,
) {
  val startMarkupRange: ClosedRange<Int>? get() {
    if (markup == null || !containStartMarkup) return null
    return (contentRange.start - markup.startText.length)..contentRange.start
  }
  val endMarkupRange: ClosedRange<Int>? get() {
    if (markup == null || !containEndMarkup) return null
    return contentRange.endInclusive..(contentRange.endInclusive + markup.endText.length)
  }
  val fillContentRange: ClosedRange<Int> get() {
    if (markup == null) return contentRange
    val startOffset = if (containStartMarkup) -markup.startText.length else 0
    val endOffset = if (containEndMarkup) markup.endText.length else 0
    return (contentRange.start + startOffset)..(contentRange.endInclusive + endOffset)
  }
}

fun List<ParseResult<PairWikitextMarkup>>.mergeInlineParseResult(
  other: List<ParseResult<InlineWikitextMarkup>>
): List<ParseResult<out TintableWikitextMarkup>> {
  val mergedList: MutableList<ParseResult<out TintableWikitextMarkup>> = this.toMutableList()
  for (otherItem in other) {
    val indexOfStartOverlapElement = this.indexOfFirst { otherItem.fillContentRange.contains(it.contentRange.start) }
    val indexOfEndOverlapElement = this.indexOfFirst { otherItem.fillContentRange.contains(it.contentRange.endInclusive) }

    if (indexOfEndOverlapElement == indexOfEndOverlapElement) {
      val overlapElement = this[indexOfEndOverlapElement]
      val (
        leftContentOfOverlapElement,
        rightContentOfOverlapElement
      ) = overlapElement.content.split(otherItem.markup!!.regex, 1)
      val newOverlapElements = listOf<ParseResult<out TintableWikitextMarkup>>(
        overlapElement.copy(
          content = leftContentOfOverlapElement,
          contentRange = overlapElement.contentRange.start..(overlapElement.contentRange.start + leftContentOfOverlapElement.length),
          containEndMarkup = false
        ),
        otherItem,
        overlapElement.copy(
          content = rightContentOfOverlapElement,
          contentRange = (overlapElement.contentRange.endInclusive - rightContentOfOverlapElement.length)..overlapElement.contentRange.endInclusive,
          containStartMarkup = false
        )
      )
      mergedList.removeAt(indexOfEndOverlapElement)
      mergedList.addAll(indexOfEndOverlapElement - 1, newOverlapElements)
    } else {
//      val startOverlapElement = this[indexOfStartOverlapElement]
//      val (
//        leftContentOfStartOverlapElement,
//        rightContentOfStartOverlapElement
//      ) = startOverlapElement.content.split(otherItem.markup!!.startText)
//      val newStartElement = startOverlapElement.copy(content = leftContentOfStartOverlapElement)
//
//      val endOverlapElement = this[indexOfEndOverlapElement]

    }
  }

  return mergedList
}

//fun List<ParseResult>.update(
//  startIndex: Int,
//  content: String? = null,
//  deleteCount: Int = 0,
//): List<ParseResult> {
//  if ()
//}

private fun AnnotatedString.Builder.tintTextByMarkup(parseResult: ParseResult<out TintableWikitextMarkup>) {
  if (parseResult.markup == null) {
    append(parseResult.content)
  } else {
    if (parseResult.containStartMarkup) {
      withStyle(parseResult.markup.style) { append(parseResult.markup.startText) }
    }
    withStyle(parseResult.markup.contentStyle) { append(parseResult.content) }
    if (parseResult.containEndMarkup) {
      withStyle(parseResult.markup.style) { append(parseResult.markup.endText) }
    }
  }
}