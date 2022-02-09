package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util

fun List<ParseResult<PairWikitextMarkup>>.mergeInlineParseResult(
  inlineParseResult: List<ParseResult<InlineWikitextMarkup>>
): List<ParseResult<out TintableWikitextMarkup>> {
  val mergedList: MutableList<ParseResult<out TintableWikitextMarkup>> = this.toMutableList()
  for (inlineParseResultItem in inlineParseResult) {
    val indexOfStartOverlapElement = mergedList.indexOfFirst { it.fullContentRange.contains(inlineParseResultItem.fullContentRange.start) }
    val indexOfEndOverlapElement = mergedList.indexOfFirst { it.fullContentRange.contains(inlineParseResultItem.fullContentRange.endInclusive) }

    if (indexOfStartOverlapElement == indexOfEndOverlapElement) {
      val overlapElement = mergedList[indexOfEndOverlapElement]

      val (
        leftContentOfOverlapElement,
        rightContentOfOverlapElement
      ) = overlapElement.content.split(inlineParseResultItem.markup!!.regex, 2)
      val newOverlapElements = mutableListOf<ParseResult<out TintableWikitextMarkup>>().apply {
        if (leftContentOfOverlapElement != "" || overlapElement.containStartMarkup || overlapElement.containEndMarkup) {
          this.add(overlapElement.copy(
            content = leftContentOfOverlapElement,
            contentRange = overlapElement.contentRange.start until (overlapElement.contentRange.start + leftContentOfOverlapElement.length),
            containEndMarkup = false
          ))
        }
        this.add(inlineParseResultItem)
        if (rightContentOfOverlapElement != "" || overlapElement.containStartMarkup || overlapElement.containEndMarkup) {
          this.add(overlapElement.copy(
            content = rightContentOfOverlapElement,
            contentRange = (overlapElement.contentRange.endInclusive - rightContentOfOverlapElement.length + 1)..overlapElement.contentRange.endInclusive,
            containStartMarkup = false
          ))
        }
      }
      mergedList.removeAt(indexOfEndOverlapElement)
      mergedList.addAll(indexOfEndOverlapElement, newOverlapElements)
    } else {
      val startOverlapElement = mergedList[indexOfStartOverlapElement]
      val endOverlapElement = mergedList[indexOfEndOverlapElement]
      val parsedTitleContent = mergedList.subList(indexOfStartOverlapElement + 1, indexOfEndOverlapElement)
        .map {
          if (it.markup == null)
            (it as ParseResult<InlineWikitextMarkup>).copy(markup = inlineParseResultItem.markup)
          else it
        }
      val (
        leftContentOfStartOverlapElement,
        rightContentOfStartOverlapElement
      ) = startOverlapElement.content.split(inlineParseResultItem.markup!!.startText, limit = 2)
      val (
        leftContentOfEndOverlapElement,
        rightContentOfEndOverlapElement,
      ) = endOverlapElement.content.split(inlineParseResultItem.markup.endText, limit = 2)

      val newOverlapElements = mutableListOf<ParseResult<out TintableWikitextMarkup>>().apply {
        if (leftContentOfStartOverlapElement != "" || startOverlapElement.containStartMarkup || startOverlapElement.containEndMarkup) {
          this.add(startOverlapElement.copy(
            content = leftContentOfStartOverlapElement,
            contentRange = startOverlapElement.contentRange.start until startOverlapElement.contentRange.start + leftContentOfStartOverlapElement.length,
            containEndMarkup = false
          ))
        }
        this.add(inlineParseResultItem.copy(
          content = "",
          contentRange = inlineParseResultItem.fullContentRange.start..inlineParseResultItem.fullContentRange.start,
          containStartMarkup = true,
          containEndMarkup = false,
          suffix = null
        ))
        if (rightContentOfStartOverlapElement != "") {
          this.add(inlineParseResultItem.copy(
            content = rightContentOfStartOverlapElement,
            contentRange = inlineParseResultItem.contentRange.start until inlineParseResultItem.contentRange.start + rightContentOfStartOverlapElement.length,
            containStartMarkup = false,
            containEndMarkup = false,
            suffix = null
          ))
        }

        this.addAll(parsedTitleContent)

        if (inlineParseResultItem.markup is InlinePairWikitextMarkup) {
          if (leftContentOfEndOverlapElement != "") {
            this.add(inlineParseResultItem.copy(
              content = leftContentOfEndOverlapElement,
              contentRange = endOverlapElement.contentRange.start until (endOverlapElement.contentRange.start + leftContentOfEndOverlapElement.length),
              containStartMarkup = false,
              containEndMarkup = false,
              suffix = null
            ))
          }

          this.add(inlineParseResultItem.copy(
            content = "",
            contentRange = inlineParseResultItem.contentRange.endInclusive..inlineParseResultItem.contentRange.endInclusive,
            containStartMarkup = false,
            containEndMarkup = true,
          ))

          if (rightContentOfEndOverlapElement != "" || endOverlapElement.containEndMarkup) {
            this.add(endOverlapElement.copy(
              content = rightContentOfEndOverlapElement,
              contentRange = inlineParseResultItem.fullContentRange.endInclusive until
                (inlineParseResultItem.fullContentRange.endInclusive + rightContentOfEndOverlapElement.length),
              containStartMarkup = false
            ))
          }
        } else {
          this.add(if (endOverlapElement.markup == null)
            (endOverlapElement as ParseResult<InlineWikitextMarkup>).copy(markup = inlineParseResultItem.markup)
          else endOverlapElement)
        }
      }

      val elementForInsertTargetPosInBeforeDelete = mergedList.getOrNull(indexOfStartOverlapElement - 1)
      mergedList.removeAll(mergedList.subList(indexOfStartOverlapElement, indexOfEndOverlapElement + 1))
      val insertTargetPos = if (elementForInsertTargetPosInBeforeDelete == null) 0 else mergedList.indexOf(elementForInsertTargetPosInBeforeDelete) + 1
      mergedList.addAll(insertTargetPos, newOverlapElements)
    }
  }

  return mergedList
}