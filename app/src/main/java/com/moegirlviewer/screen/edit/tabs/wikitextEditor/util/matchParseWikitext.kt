package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util

import androidx.compose.ui.text.SpanStyle

fun matchParseWikitext(wikitext: String): List<ParseResult<InlineWikitextMarkup>> {
  val resultList = mutableListOf<ParseResult<InlineWikitextMarkup>>()
  var currentText: String? = wikitext

  while (currentText != null) {
    val matchResult = InlineMarkupMatcher.matchMarkup(currentText)
    if (matchResult != null) {
      when(matchResult.markup) {
        is InlineEqualWikitextMarkup -> {
          val contentRange =
            (matchResult.range.start - matchResult.markup.startText.length)..
            (matchResult.range.endInclusive - matchResult.markup.endText.length)

          resultList.add(ParseResult(
            content = matchResult.content,
            contentRange = contentRange,
            markup = matchResult.markup,
            containStartMarkup = true,
            containEndMarkup = true
          ))
        }
        is InlineSingleWikitextMarkup -> {
          val contentRange =
            (matchResult.range.start - matchResult.markup.startText.length)..
            matchResult.range.endInclusive

          resultList.add(ParseResult(
            content = matchResult.content,
            contentRange = contentRange,
            markup = matchResult.markup,
            containStartMarkup = true
          ))
        }
      }

      currentText = currentText.substring(matchResult.range.endInclusive + 1)
    } else {
      currentText = null
    }
  }

  return resultList
}

private object InlineMarkupMatcher {
  fun matchMarkup(text: String): MarkupMatchResult? {
    var regexMatchResult: MatchResult? = null
    val matchedMarkup = matchParsingMarkupList.firstOrNull {
      val findResult = it.regex.find(text)
      if (findResult != null) regexMatchResult = findResult
      findResult != null
    }

    return if (matchedMarkup != null) MarkupMatchResult(
      content = regexMatchResult!!.groupValues[1],
      range = regexMatchResult!!.range,
      markup = matchedMarkup
    ) else null
  }
}

abstract class InlineWikitextMarkup(
  override val startText: String,
  override val endText: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : WikitextMarkup(style, contentStyle), TintableWikitextMarkup {
  abstract val regex: Regex
}

open class InlinePairWikitextMarkup(
  startText: String,
  endText: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : InlineWikitextMarkup(startText, endText, style, contentStyle) {
  override val regex = run {
    val regexStartText = startText
    val regexEndText = endText
    Regex("""^$regexStartText([\s\S]+)$regexEndText\s+?$""", RegexOption.MULTILINE)
  }
}

class InlineEqualWikitextMarkup(
  text: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : InlinePairWikitextMarkup(text, text, style, contentStyle)

class InlineSingleWikitextMarkup(
  startText: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : InlineWikitextMarkup(startText, "\n", style, contentStyle), TintableWikitextMarkup {
  override val regex get() = run {
    val regexStartText = Regex.escape(startText)
    Regex("""^$regexStartText([\s\S]+?)$""", RegexOption.MULTILINE)
  }
}

private class MarkupMatchResult(
  val content: String,
  val range: ClosedRange<Int>,
  val markup: InlineWikitextMarkup,
)