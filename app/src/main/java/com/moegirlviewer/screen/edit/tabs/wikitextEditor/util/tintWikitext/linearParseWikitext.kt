package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext

import androidx.compose.ui.text.SpanStyle

internal fun linearParseWikitext(wikitext: String): List<ParseResult<PairWikitextMarkup>> {
  val wikitextLength = wikitext.length
  var startCursor = 0
  var endCursor = 0
  var markupTextCache = ""
  val stackForStartMarkupMatch = mutableListOf<PairWikitextMarkup>()
  var skipStartMarkupMatching = false  // 为ture时，跳过开始EqualWikitextMarkup的开始匹配，让结束匹配来处理
  var skipCursorIncrement = false

  val resultList = mutableListOf<ParseResult<PairWikitextMarkup>>()
  while(endCursor < wikitextLength) {
    if (skipCursorIncrement) {
      skipCursorIncrement = false
    } else {
      markupTextCache += wikitext[endCursor]
      endCursor++
    }

    // 起始标记匹配
    if (markupTextCache.length >= PairMarkupMatcher.minMarkupTextLength) {
      try {
        val marchedFirstMarkup = PairMarkupMatcher.matchStart(markupTextCache, wikitext, endCursor)
        if (marchedFirstMarkup is EqualWikitextMarkup && skipStartMarkupMatching) {
          skipStartMarkupMatching = false
        } else {
          val startMarkupPreviousText = wikitext.substring(startCursor, endCursor - marchedFirstMarkup.startText.length)
          if (startMarkupPreviousText != "") {
            val parseResultOfStartMarkupPreviousText = ParseResult(
              content = startMarkupPreviousText,
              contentRange = startCursor until endCursor - marchedFirstMarkup.startText.length,
              markup = if (stackForStartMarkupMatch.isNotEmpty()) stackForStartMarkupMatch.last() else null,
            )

            resultList.add(parseResultOfStartMarkupPreviousText)
          }

          val parseResultOfStartMarkup = ParseResult(
            markup = marchedFirstMarkup,
            startMarkup = true,
            contentOriginalPos = endCursor - marchedFirstMarkup.startText.length + marchedFirstMarkup.startText.length
          )

          resultList.add(parseResultOfStartMarkup)

          stackForStartMarkupMatch.add(marchedFirstMarkup)
          if (marchedFirstMarkup is EqualWikitextMarkup) skipStartMarkupMatching = true
          startCursor = endCursor
          markupTextCache = ""
          continue
        }
      }
      catch (e: NoSuchElementException) {}
      catch (e: ProbeHitException) {
        endCursor = e.probePosition
        markupTextCache = e.probeText
        skipCursorIncrement = true
        continue
      }

      // 结束标记匹配
      try {
        val marchedEndMarkup = PairMarkupMatcher.matchEnd(markupTextCache, wikitext, endCursor)
        val isEqualWikiTextMarkup = marchedEndMarkup is EqualWikitextMarkup
        if (stackForStartMarkupMatch.last() == marchedEndMarkup) {
          resultList.addAll(ParseResult(
            content = wikitext.substring(startCursor, endCursor - marchedEndMarkup.endText.length),
            contentRange = startCursor until endCursor - marchedEndMarkup.endText.length,
            markup = marchedEndMarkup,
          ).expand(
            containEndMarkup = true
          ))
          startCursor = endCursor
          markupTextCache = ""
          if (isEqualWikiTextMarkup) {
            val indexOfWillDeleteItemIndex = stackForStartMarkupMatch.indexOfLast { it == marchedEndMarkup }
            if (indexOfWillDeleteItemIndex != -1) stackForStartMarkupMatch.removeAt(indexOfWillDeleteItemIndex)
          } else {
            stackForStartMarkupMatch.removeLast()
          }
        }
      }
      catch (e: NoSuchElementException) {}
      catch (e: ProbeHitException) {
        endCursor = e.probePosition
        markupTextCache = e.probeText
        skipCursorIncrement = true
        continue
      }
    }
  }

  if (markupTextCache.isNotEmpty()) {
    if (stackForStartMarkupMatch.isNotEmpty()) {
      resultList.add(ParseResult(
        content = markupTextCache,
        contentRange = startCursor until endCursor,
        markup = stackForStartMarkupMatch.last(),
      ))
    } else {
      resultList.add(ParseResult(
        markupTextCache,
        contentRange = startCursor until endCursor
      ))
    }
  }

  return resultList
}

private object PairMarkupMatcher {
  val minMarkupTextLength =
    linearParsingMarkupList.minOf { if (it.startText.length < it.endText.length) it.startText.length else it.endText.length }
  val maxMarkupTextLength =
    linearParsingMarkupList.maxOf { if (it.startText.length > it.endText.length) it.startText.length else it.endText.length }
  fun matchStart(text: String, fullText: String, cursor: Int): PairWikitextMarkup {
    val foundMarkup = linearParsingMarkupList.first { it.matchStart(text) }
    probe(ProbeType.START, foundMarkup.startText, fullText, cursor)
    return foundMarkup
  }
  fun matchEnd(text: String, fullText: String, cursor: Int): PairWikitextMarkup {
    val foundMarkup = linearParsingMarkupList.first { it.matchEnd(text) }
    probe(ProbeType.END, foundMarkup.endText, fullText, cursor)
    return foundMarkup
  }

  // 探测文本前方是否有更长匹配的语法，典型栗子是防止在匹配[[]]时[]先被解析，导致[[]]被解析成[]
  fun probe(
    type: ProbeType,
    text: String,
    fullText: String,
    cursor: Int,
  ) {
    var probeIndex = 0
    var probeText = text
    class HitProbe(
      val text: String,
      val position: Int
    )
    var hitProbe: HitProbe? = null
    while (probeText.length <= maxMarkupTextLength && (cursor + probeIndex < fullText.length - 1)) {
      // cursor是用来切substring的，本身就比当前文字的index大1，所以probeIndex是0时拿到的也是下一个字符
      probeText += fullText[cursor + probeIndex]

      val result = if (type == ProbeType.START) {
        linearParsingMarkupList.indexOfFirst { it.startText == probeText }
      } else {
        linearParsingMarkupList.indexOfFirst { it.endText == probeText }
      }

      if (result != -1) {
        hitProbe = HitProbe(
          text = probeText,
          position = cursor + probeIndex + 1
        )
      }

      probeIndex++
    }

    if (hitProbe != null) throw ProbeHitException(
      probeText = hitProbe.text,
      probePosition = hitProbe.position
    )
  }
}

private enum class ProbeType {
  START, END
}

// 探针命中，返回当时探针的位置，游标应该直接跳到这个位置
class ProbeHitException(
  val probeText: String,
  val probePosition: Int
) : Exception()

open class PairWikitextMarkup(
  override val startText: String,
  override val endText: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : WikitextMarkup(style, contentStyle), TintableWikitextMarkup {
  fun matchStart(text: String) = text.takeLast(startText.length) == startText
  fun matchEnd(text: String) = text.takeLast(endText.length) == endText
}

class EqualWikitextMarkup(
  text: String,
  style: SpanStyle,
  contentStyle: SpanStyle = style
) : PairWikitextMarkup(text, text, style, contentStyle)