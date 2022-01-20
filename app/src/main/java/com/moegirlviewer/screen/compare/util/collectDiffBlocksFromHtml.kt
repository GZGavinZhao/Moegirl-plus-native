package com.moegirlviewer.screen.compare.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun collectDiffBlocksFormHtml(html: String): List<DiffBlock> {
  val htmlDoc = Jsoup.parse(html)
  val diffBlocks = mutableListOf<DiffBlock>()

  htmlDoc.getElementsByTag("tr").forEach {
    val diffLineNoEl = it.selectFirst(".diff-lineno")
    if (diffLineNoEl != null) {
      val leftLineHint = it.selectFirst(".diff-lineno:first-child")!!.text()
      val rightLineHint = it.selectFirst(".diff-lineno:last-child")!!.text()

      val diffBlock = DiffBlock(
        left = DiffLine(leftLineHint),
        right = DiffLine(rightLineHint)
      )

      diffBlocks.add(diffBlock)
    } else {
      // 如果一行中出现了上下文，那一定会左右都有内容，且没有修改的内容标志
      val diffContextEls = it.getElementsByClass("diff-context")
      if (diffContextEls.isNotEmpty()) {
        diffBlocks.last().left.rows.add(
          DiffRow(
            marker = DiffRowMarker.NONE,
            content = mutableListOf(DiffRowContent(
              type = DiffRowContentType.PLAIN,
              text = diffContextEls.first()!!.text()
            ))
          )
        )
        diffBlocks.last().right.rows.add(
          DiffRow(
            marker = DiffRowMarker.NONE,
            content = mutableListOf(DiffRowContent(
              type = DiffRowContentType.PLAIN,
              text = diffContextEls.last()!!.text()
            ))
          )
        )

        return@forEach
      }

      // 解析内容行
      val leftRow = DiffRow()
      val rightRow = DiffRow()

      val deletedLineEl = it.selectFirst(".diff-deletedline")
      val addedLineEl = it.selectFirst(".diff-addedline")

      if (deletedLineEl != null) {
        leftRow.marker = DiffRowMarker.MINUS
        val deletedLineContentNodeEls = deletedLineEl.selectFirst("div")!!.childNodes()
        deletedLineContentNodeEls.forEach { node ->
          val diffContent = if (node is TextNode) {
            DiffRowContent(
              type = DiffRowContentType.PLAIN,
              text = node.text()
            )
          } else {
            DiffRowContent(
              type = DiffRowContentType.DELETE,
              text = (node as Element).text()
            )
          }

          leftRow.content.add(diffContent)
        }
      }

      if (addedLineEl != null) {
        rightRow.marker = DiffRowMarker.PLUS
        val addedLineContentNodeEls = addedLineEl.selectFirst("div")!!.childNodes()
        addedLineContentNodeEls.forEach { node ->
          val diffContent = if (node is TextNode) {
            DiffRowContent(
              type = DiffRowContentType.PLAIN,
              text = node.text()
            )
          } else {
            DiffRowContent(
              type = DiffRowContentType.ADD,
              text = (node as Element).text()
            )
          }

          rightRow.content.add(diffContent)
        }
      }

      diffBlocks.last().left.rows.add(leftRow)
      diffBlocks.last().right.rows.add(rightRow)
    }
  }

  return diffBlocks
}

class DiffBlock(
  val left: DiffLine,
  val right: DiffLine
)

class DiffLine(
  val lineHint: String,
  val rows: MutableList<DiffRow> = mutableListOf()
)

class DiffRow(
  var marker: DiffRowMarker = DiffRowMarker.NONE,
  val content: MutableList<DiffRowContent> = mutableListOf()
)

class DiffRowContent(
  val type: DiffRowContentType,
  val text: String
)

enum class DiffRowMarker {
  PLUS, MINUS, NONE
}

enum class DiffRowContentType {
  PLAIN, ADD, DELETE
}