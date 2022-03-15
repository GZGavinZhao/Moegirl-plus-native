package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext

import androidx.compose.ui.text.SpanStyle

internal fun matchParseWikitext(wikitext: String): List<FullLineParseResult> {
  val resultList = mutableListOf<FullLineParseResult>()
  var startIndex = 0

  while (startIndex != -1) {
    val matchResult = InlineMarkupMatcher.matchMarkup(wikitext, startIndex)
    if (matchResult != null) {
      when(matchResult.markup) {
        is InlineEqualWikitextMarkup -> {
          val contentRange =
            (matchResult.range.start + matchResult.markup.startText.length)..
            (matchResult.range.endInclusive - matchResult.markup.endText.length)

          resultList.add(
            FullLineParseResult(
            content = matchResult.content,
            contentRange = contentRange,
            markup = matchResult.markup,
            containStartMarkup = true,
            containEndMarkup = true,
            suffixSpace = matchResult.emptyStringInMarkupEnd
          )
          )
        }
        is InlineSingleWikitextMarkup -> {
          val contentRange =
            (matchResult.range.start + matchResult.markup.startText.length)..
            matchResult.range.endInclusive

          resultList.add(
            FullLineParseResult(
            content = matchResult.content,
            contentRange = contentRange,
            markup = matchResult.markup,
            containStartMarkup = true,
            suffixSpace = matchResult.emptyStringInMarkupEnd
          )
          )
        }
      }

      startIndex = matchResult.range.endInclusive + 1
    } else {
      startIndex = -1
    }
  }

  return resultList
}

private object InlineMarkupMatcher {
  fun matchMarkup(text: String, startIndex: Int): MarkupMatchResult? {
    val matchedMarkup = matchParsingMarkupList
      .mapNotNull {
        val result = it.regex.find(text, startIndex)
        if (result != null) it to result else null
      }
      .minByOrNull { it.second.range.first }

    return if (matchedMarkup != null) MarkupMatchResult(
      content = matchedMarkup.second.groupValues[1],
      range = matchedMarkup.second.range,
      markup = matchedMarkup.first,
      emptyStringInMarkupEnd = matchedMarkup.second.groupValues.getOrNull(2).run {
        if (this == null) return@run null
        return@run if (this == "") null else this
      }
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
    val regexStartText = Regex.escape(startText)
    val regexEndText = Regex.escape(endText)
    Regex("""^$regexStartText([\s\S]+?)$regexEndText(\s*?)$""", RegexOption.MULTILINE)
  }
}

class InlineEqualWikitextMarkup(
  text: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : InlinePairWikitextMarkup(text, text, style, contentStyle)

class InlineSingleWikitextMarkup(
  text: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : InlineWikitextMarkup(text, "", style, contentStyle), TintableWikitextMarkup {
  override val regex get() = run {
    val regexStartText = Regex.escape(startText)
    Regex("""^$regexStartText([\s\S]+?)$""", RegexOption.MULTILINE)
  }
}

data class FullLineParseResult(
  val content: String? = null,
  val contentRange: ClosedRange<Int>,
  val markup: InlineWikitextMarkup,
  val containStartMarkup: Boolean = false,
  val containEndMarkup: Boolean = false,
  val prefixSpace: String? = null,
  val suffixSpace: String? = null,
) {
  val fullContentRange: ClosedRange<Int> = (contentRange.start - markup.startText.length)..(contentRange.endInclusive + markup.endText.length)

  fun toTintableParseResultList() = mutableListOf<ParseResult<out TintableWikitextMarkup>>().apply {
    if (prefixSpace != null) {
      val startMarkupLength = if (containStartMarkup) markup.startText.length else 0
      this.add(ParseResult(
        content = prefixSpace,
        contentRange = (contentRange.start - prefixSpace.length - startMarkupLength)..contentRange.start
      ))
    }
    if (containStartMarkup) this.add(ParseResult(
      markup = markup,
      contentOriginalPos = contentRange.start,
      startMarkup = true
    ))
    if (content != null) {
      this.add(ParseResult(
        content = content,
        contentRange = contentRange,
        markup = markup
      ))
    }
    if (containEndMarkup) this.add(ParseResult(
      markup = markup,
      contentOriginalPos = contentRange.endInclusive,
      startMarkup = false
    ))
    if (suffixSpace != null) {
      val endMarkupLength = if (containEndMarkup) markup.endText.length else 0
      this.add(ParseResult(
        content = suffixSpace,
        contentRange = contentRange.endInclusive..(contentRange.endInclusive + suffixSpace.length + endMarkupLength)
      ))
    }
  }
}

private class MarkupMatchResult(
  val content: String,
  val range: ClosedRange<Int>,
  val markup: InlineWikitextMarkup,
  val emptyStringInMarkupEnd: String?
)