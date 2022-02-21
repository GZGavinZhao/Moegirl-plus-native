package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext

fun MutableList<ParseResult<out TintableWikitextMarkup>>.modify(diff: DiffBlock, hacked: Boolean = false): List<ParseResult<out TintableWikitextMarkup>> {
  if (hacked) {
    if (diff.type == DiffType.PLUS) {
      val indexOfTargetElement = this.indexOfFirst { it.contentRange.contains(diff.position) }
      val targetElement = this[indexOfTargetElement]
      val relativeStartPosition = diff.position - targetElement.contentRange.start
//      val relativeEndPosition = relativeStartPosition + diff.content.length
      val leftContentOfStartPos = targetElement.content.substring(0, relativeStartPosition)
      val rightContentOfEndPos = targetElement.content.substring(relativeStartPosition)

      this[indexOfTargetElement] = targetElement.copy(
        content = leftContentOfStartPos + diff.content + rightContentOfEndPos,
        contentRange = targetElement.contentRange.start..(targetElement.contentRange.endInclusive + diff.content.length)
      )

      this.subList(indexOfTargetElement + 1, this.size)
        .toMutableList()
        .replaceAll { it.copy(
          contentRange = (it.contentRange.start + diff.content.length)..(it.contentRange.endInclusive + diff.content.length)
        ) }
    } else {
      val endPosition = diff.position
      val startPosition = endPosition - diff.content.length + 1
      val indexOfTargetResultStartElement = this.indexOfFirst { it.contentRange.contains(startPosition) }
      val indexOfTargetResultEndElement = this.indexOfFirst { it.contentRange.contains(endPosition) }

      // 变化内容处于同一节点
      if (indexOfTargetResultStartElement == indexOfTargetResultEndElement) {
        val activeResultElement = this[indexOfTargetResultStartElement]
        val relativeStartPosition = startPosition - activeResultElement.contentRange.start
        val relativeEndPosition = endPosition - activeResultElement.contentRange.start
        val newElement = activeResultElement.copy(
          content = activeResultElement.content.removeRange(relativeStartPosition..relativeEndPosition)
        )
        if (newElement.content.isNotEmpty()) {
          this[indexOfTargetResultStartElement] = newElement
        } else {
          this.removeAt(indexOfTargetResultStartElement)
        }

        this.subList(indexOfTargetResultStartElement + 1, this.size)
          .toMutableList()
          .replaceAll {
            it.copy(
              contentRange = (it.contentRange.start - diff.content.length)..(it.contentRange.endInclusive - diff.content.length)
            )
          }
      } else {

      }
    }
  } else {
  //  when (diff.content) {
  //
  //  }
    val indexOfModifyTarget = this.indexOfFirst { it.contentRange.contains(diff.position) }
    val modifyTarget = this[indexOfModifyTarget]
  }

  return this
}