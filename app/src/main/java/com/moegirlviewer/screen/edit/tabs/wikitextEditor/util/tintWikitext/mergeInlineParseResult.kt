package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext

internal fun List<ParseResult<PairWikitextMarkup>>.mergeInlineParseResult(
  inlineParseResult: List<FullLineParseResult>
): List<ParseResult<out TintableWikitextMarkup>> {
  val mergedList: MutableList<ParseResult<out TintableWikitextMarkup>> = this.toMutableList()
  for (inlineParseResultItem in inlineParseResult) {
    val indexOfStartOverlapElement = mergedList.indexOfFirst { it.contentRange.contains(inlineParseResultItem.fullContentRange.start) }
    val indexOfEndOverlapElement = mergedList.indexOfFirst { it.contentRange.contains(inlineParseResultItem.fullContentRange.endInclusive) }

    if (inlineParseResult.indexOf(inlineParseResultItem) == 5) {
      true
    }

    fun List<String>.pad(size: Int, value: String = ""): List<String> {
      val result = this.toMutableList()
      while (result.size < size) result.add(value)
      return result
    }

    if (indexOfStartOverlapElement == indexOfEndOverlapElement) {
      val overlapElement = mergedList[indexOfEndOverlapElement]

      val (
        leftContentOfOverlapElement,
        rightContentOfOverlapElement
      ) = overlapElement.content.split(inlineParseResultItem.markup.regex, 2).pad(2)
      val newOverlapElements = mutableListOf<ParseResult<out TintableWikitextMarkup>>().apply {
        if (leftContentOfOverlapElement != "") {
          this.add(overlapElement.copy(
            content = leftContentOfOverlapElement,
            contentRange = overlapElement.contentRange.start until (overlapElement.contentRange.start + leftContentOfOverlapElement.length),
          ))
        }
        this.addAll(inlineParseResultItem.toTintableParseResultList())
        if (rightContentOfOverlapElement != "") {
          this.add(overlapElement.copy(
            content = rightContentOfOverlapElement,
            contentRange = (overlapElement.contentRange.endInclusive - rightContentOfOverlapElement.length + 1)..overlapElement.contentRange.endInclusive,
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
      ) = startOverlapElement.content.split(inlineParseResultItem.markup.startText, limit = 2).pad(2)
      val (
        leftContentOfEndOverlapElement,
        rightContentOfEndOverlapElement,
      ) = endOverlapElement.content.split(inlineParseResultItem.markup.endText, limit = 2).pad(2)

      val newOverlapElements = mutableListOf<ParseResult<out TintableWikitextMarkup>>().apply {
        if (leftContentOfStartOverlapElement != "") {
          this.add(startOverlapElement.copy(
            content = leftContentOfStartOverlapElement,
            contentRange = startOverlapElement.contentRange.start until startOverlapElement.contentRange.start + leftContentOfStartOverlapElement.length,
          ))
        }
        this.addAll(inlineParseResultItem.copy(
          content = null,
          contentRange = inlineParseResultItem.contentRange.start..inlineParseResultItem.contentRange.start,
          suffixSpace = null,
          containStartMarkup = true,
          containEndMarkup = false
        ).toTintableParseResultList())
        if (rightContentOfStartOverlapElement != "") {
          this.addAll(inlineParseResultItem.copy(
            content = rightContentOfStartOverlapElement,
            contentRange = inlineParseResultItem.contentRange.start until inlineParseResultItem.contentRange.start + rightContentOfStartOverlapElement.length,
            suffixSpace = null,
            containStartMarkup = false,
            containEndMarkup = false
          ).toTintableParseResultList())
        }

        this.addAll(parsedTitleContent)

        if (inlineParseResultItem.markup is InlinePairWikitextMarkup) {
          if (leftContentOfEndOverlapElement != "") {
            this.addAll(inlineParseResultItem.copy(
              content = leftContentOfEndOverlapElement,
              contentRange = endOverlapElement.contentRange.start until (endOverlapElement.contentRange.start + leftContentOfEndOverlapElement.length),
              suffixSpace = null,
              containStartMarkup = false,
              containEndMarkup = false
            ).toTintableParseResultList())
          }

          this.addAll(inlineParseResultItem.copy(
            content = null,
            contentRange = inlineParseResultItem.contentRange.endInclusive..inlineParseResultItem.contentRange.endInclusive,
            containStartMarkup = false,
            containEndMarkup = true
          ).toTintableParseResultList())

          if (rightContentOfEndOverlapElement != "") {
            this.add(endOverlapElement.copy(
              content = rightContentOfEndOverlapElement,
              contentRange = inlineParseResultItem.fullContentRange.endInclusive until
                (inlineParseResultItem.fullContentRange.endInclusive + rightContentOfEndOverlapElement.length),
            ))
          }
        } else {
          this.add(if (endOverlapElement.markup == null)
            (endOverlapElement as ParseResult<InlineWikitextMarkup>).copy(markup = inlineParseResultItem.markup)
          else endOverlapElement)
        }
      }

      val elementForInsertTargetPosInBeforeDelete = mergedList.getOrNull(indexOfStartOverlapElement - 1)
      val newMergedList = mergedList.filterIndexed { index, _ -> (indexOfStartOverlapElement..indexOfEndOverlapElement).contains(index).not() }
      mergedList.clear()
      mergedList.addAll(newMergedList)
//      mergedList.removeAll(mergedList.subList(indexOfStartOverlapElement, indexOfEndOverlapElement + 1))
      val insertTargetPos = if (elementForInsertTargetPosInBeforeDelete == null) 0 else mergedList.indexOf(elementForInsertTargetPosInBeforeDelete) + 1
      mergedList.addAll(insertTargetPos, newOverlapElements)
    }
  }

  return mergedList
}