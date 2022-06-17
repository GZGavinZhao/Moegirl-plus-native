package com.moegirlviewer.screen.compare.util

import org.jsoup.nodes.Comment
import org.jsoup.nodes.Element

fun Element.parseLinearDiff(): List<LinearDiffRows> {
  return this.children().fold<Element, MutableList<LinearDiffRows>>(mutableListOf()) { result, item ->
    if (item.hasClass("mw-diff-inline-header")) {
      val rowsRangeCommentEl = item.childNodes().first() as Comment
      val rowsRangeValues = Regex("""^LINES (\d+),(\d+)$""").find(rowsRangeCommentEl.data.trim())!!.groupValues
      val rowsRange = rowsRangeValues[1].toInt()..rowsRangeValues[2].toInt()
      val newLineRows = LinearDiffRows(rowsRange)
      result.add(newLineRows)
    } else {
      result.last().sentences.addAll(item.toLinearDiffSentence())
    }

    result
  }
    .onEach {
      // 判断如果为rows的最后一行，且为DELETED，则去掉在toLinearDiffSentence中额外添加的换行符
      val lastSentence = it.sentences.last()
      if (lastSentence.type != LinearDiffType.DELETED) lastSentence.text = lastSentence.text.substring(0, lastSentence.text.length - 1)
    }
}

private fun Element.toLinearDiffSentence(): List<LinearDiffSentence> {
  if (this.hasClass("mw-diff-inline-changed")) {
    return this.children().map {
      val type = if (it.tagName() == "del") LinearDiffType.DELETED else LinearDiffType.ADDED
      val text = it.text()
      return@map LinearDiffSentence(type, text)
    }
  } else {
    val type = when {
      this.hasClass("mw-diff-inline-added") -> LinearDiffType.ADDED
      else -> LinearDiffType.CONTEXT
    }
    val text = this.text()
    if (text == "") return emptyList()
    return listOf(LinearDiffSentence(type, text + "\n"))
  }
}

enum class LinearDiffType {
  CONTEXT,
  ADDED,
  DELETED
}

class LinearDiffSentence(
  val type: LinearDiffType,
  var text: String
)

class LinearDiffRows(
  val range: IntRange,
  val sentences: MutableList<LinearDiffSentence> = mutableListOf()
)