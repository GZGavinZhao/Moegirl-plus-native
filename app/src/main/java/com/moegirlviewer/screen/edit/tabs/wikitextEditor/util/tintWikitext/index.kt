package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withStyle
import com.moegirlviewer.util.Italic

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
    text = "'''''",
    style = SpanStyle(
      fontWeight = FontWeight.Black,
      textGeometricTransform = TextGeometricTransform.Italic()
    ),
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
      textGeometricTransform = TextGeometricTransform.Italic()
    )
  )
)

internal val matchParsingMarkupList = listOf(
  *(6 downTo 1).map {
    InlineEqualWikitextMarkup(
      text = "=".repeat(it),
      style = SpanStyle(
        color = Color(0xffCB2828),
        fontWeight = FontWeight.Black
      )
    )
  }.toTypedArray(),
  InlineSingleWikitextMarkup(
    text = "*",
    style = SpanStyle(
      color = Color(0xff6868F2),
//      fontWeight = FontWeight.Black
    ),
    contentStyle = SpanStyle(
      color = Color(0xff6868F2)
    )
  ),
  InlineSingleWikitextMarkup(
    text = "#",
    style = SpanStyle(
      color = Color(0xff6868F2),
//      fontWeight = FontWeight.Black
    ),
    contentStyle = SpanStyle(
      color = Color(0xff6868F2)
    )
  ),
  InlineSingleWikitextMarkup(
    text = ";",
    style = SpanStyle(
      color = Color(0xff6868F2),
//      fontWeight = FontWeight.Black
    ),
    contentStyle = SpanStyle(
      color = Color(0xff6868F2)
    )
  ),
)

data class TintedWikitext(
  val originalWikitext: String,
  private val cursorPosition: Int = 0,
  private val parseResult: List<ParseResult<out TintableWikitextMarkup>> = emptyList()
) {
  fun update(newWikitext: String, cursorPosition: Int) = TintedWikitext(
    originalWikitext = newWikitext,
    cursorPosition = cursorPosition,
    parseResult = tintWikitext(newWikitext)
  )

  // 高亮性能优化暂时研究不明白了，先这样吧
  fun hackedUpdate(newWikitext: String, cursorPosition: Int): TintedWikitext {
    fun defaultReturn() = this.copy(
      cursorPosition = cursorPosition
    )

    return when {
      // 内容无变化
      newWikitext == originalWikitext -> defaultReturn()

      originalWikitext.isEmpty() -> this.copy(
        originalWikitext = originalWikitext,
        cursorPosition = cursorPosition,
      )

      // 光标位置删除单个字符
      newWikitext.length == originalWikitext.length - 1 -> {
        val deletedChar = newWikitext[cursorPosition]
//        val indexOfActiveResultElement = parseResult.indexOfFirst { it.contentRange.contains(cursorPosition) }
//        val activeResultElement = parseResult[indexOfActiveResultElement]
        val mutableResult = parseResult.toMutableList()
        val diffBlock = DiffBlock(
          type = DiffType.MINUS,
          content = deletedChar.toString(),
          position = cursorPosition
        )

        TintedWikitext(
          originalWikitext = newWikitext,
          cursorPosition = cursorPosition,
          parseResult = mutableResult.modify(diffBlock, true)
        )
      }

      // 光标位置输入单个字符
      newWikitext.length == originalWikitext.length + 1 -> {
        val addedChar = newWikitext[cursorPosition - 1]
        val mutableResult = parseResult.toMutableList()
        val diffBlock = DiffBlock(
          type = DiffType.MINUS,
          content = addedChar.toString(),
          position = cursorPosition
        )

        TintedWikitext(
          originalWikitext = newWikitext,
          cursorPosition = cursorPosition,
          parseResult = mutableResult.modify(diffBlock, true)
        )
      }

      // 其他情况
      else -> {
        fun String.toLineContents() = Regex("[^\n]+\n?").findAll(this).map { it.range to it.value }.toList()
        val originalLineContents = originalWikitext.toLineContents()
        val newLineContents = newWikitext.toLineContents()
        // 变化内容都在一行
        if (originalLineContents.size == newLineContents.size) {
          val indexOfActiveLine = newLineContents.indexOfFirst { it.first.contains(cursorPosition) }
          val lineDiffs = diffWikitext(originalLineContents[indexOfActiveLine].second, newLineContents[indexOfActiveLine].second)
          val newParseResult = parseResult.toMutableList()
          for (item in lineDiffs) {
            if (item.type == DiffType.PLUS) {
              val indexOfActiveResultElement = parseResult.indexOfFirst { it.contentRange.contains(item.position) }
              val activeResultElement = parseResult[indexOfActiveResultElement]
              newParseResult[indexOfActiveResultElement] = activeResultElement.copy(
                content = activeResultElement.content + item.content
              )
            } else {
              val endPosition = item.position
              val startPosition = endPosition - item.content.length
              val indexOfActiveResultStartElement = parseResult.indexOfFirst { it.contentRange.contains(startPosition) }
              val indexOfActiveResultEndElement = parseResult.indexOfFirst { it.contentRange.contains(endPosition) }

              // 变化内容处于同一节点
              if (indexOfActiveResultStartElement == indexOfActiveResultEndElement) {
                val activeResultElement = parseResult[indexOfActiveResultStartElement]
                val relativeStartPosition = startPosition - activeResultElement.contentRange.start
                val relativeEndPosition = endPosition - activeResultElement.contentRange.start
                newParseResult[indexOfActiveResultStartElement] = activeResultElement.copy(
                  content = activeResultElement.content.removeRange(relativeStartPosition..relativeEndPosition)
                )
              } else {

              }
            }
          }

          return TintedWikitext(
            originalWikitext = newWikitext,
            cursorPosition = cursorPosition,
            parseResult = newParseResult
          )
        } else {
          // 其他情况，这时仅仅是对比内容也会花费较长时间，直接返回原文
          return defaultReturn()
        }
      }
    }
  }

  val annotatedString get() = buildAnnotatedString {
    for (item in parseResult) {
      tintTextByMarkup(item)
    }
  }
}

private fun tintWikitext(wikitext: String): List<ParseResult<out TintableWikitextMarkup>> {
  val linearParseResult = linearParseWikitext(wikitext)
  val matchParseResult = matchParseWikitext(wikitext)

  return linearParseResult.mergeInlineParseResult(matchParseResult)
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
  val content: String,
  val contentRange: ClosedRange<Int>,
  val markup: T? = null,
//  val containStartMarkup: Boolean = false,
//  val containEndMarkup: Boolean = false,
//  val prefix: String? = null,
//  val suffix: String? = null
) {
  constructor(
    markup: T?,
    contentOriginalPos: Int,
    startMarkup: Boolean,
  ) : this(
    content = (if (startMarkup) markup?.startText else markup?.endText) ?: "",
    markup = markup,
    contentRange = if (startMarkup)
      (contentOriginalPos - (markup?.startText?.length ?: 0)) until contentOriginalPos else
      (contentOriginalPos + 1)..(contentOriginalPos + (markup?.endText?.length ?: 0))
  )

  fun expand(
    containStartMarkup: Boolean = false,
    containEndMarkup: Boolean = false,
  ): List<ParseResult<T>> = mutableListOf<ParseResult<T>>().apply {
    if (containStartMarkup) this.add(ParseResult(
      markup = this@ParseResult.markup,
      contentOriginalPos = contentRange.start,
      startMarkup = true
    ))
    this.add(this@ParseResult)
    if (containEndMarkup) this.add(ParseResult(
      markup = this@ParseResult.markup,
      contentOriginalPos = contentRange.endInclusive,
      startMarkup = false
    ))
  }
//  val startMarkupRange: ClosedRange<Int>? get() {
//    if (markup == null) return null
//    return (contentRange.start - markup.startText.length)..contentRange.start
//  }
//  val endMarkupRange: ClosedRange<Int>? get() {
//    if (markup == null) return null
//    return contentRange.endInclusive..(contentRange.endInclusive + markup.endText.length)
//  }
//  val fullContentRange: ClosedRange<Int> get() {
//    if (markup == null) return contentRange
//    val startOffset = if (containStartMarkup) -markup.startText.length else 0
//    val endOffset = if (containEndMarkup) markup.endText.length else 0
//    return (contentRange.start + startOffset - (prefix?.length ?: 0))..(contentRange.endInclusive + endOffset + (suffix?.length ?: 0))
//  }
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
//    if (parseResult.prefix != null) append(parseResult.prefix)
//    if (parseResult.containStartMarkup) {
//      withStyle(parseResult.markup.style) { append(parseResult.markup.startText) }
//    }
    withStyle(parseResult.markup.contentStyle) { append(parseResult.content) }
//    if (parseResult.containEndMarkup) {
//      withStyle(parseResult.markup.style) { append(parseResult.markup.endText) }
//    }
//    if (parseResult.suffix != null) append(parseResult.suffix)
  }
}