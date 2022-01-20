package com.moegirlviewer.component.articleView.util

import org.jsoup.Jsoup

fun collectCategoryDataFromHtml(html: String): CollectedCategoryDataFromHtml {
  val htmlDoc = Jsoup.parse(html)
  val parentCategoriesContainerEl = htmlDoc.getElementById("topicpath")
  val descContainer = htmlDoc.getElementById("catmore")
  var parentCategories: List<String>? = null
  var categoryExplainPageName: String? = null

  parentCategories = parentCategoriesContainerEl
    ?.getElementsByTag("a")
    ?.map { it.text() }

  categoryExplainPageName = descContainer
    ?.getElementsByTag("a")
    ?.get(0)?.attr("title")

  return CollectedCategoryDataFromHtml(
    parentCategories = parentCategories,
    categoryExplainPageName = categoryExplainPageName
  )
}

class CollectedCategoryDataFromHtml(
  val parentCategories: List<String>?,
  val categoryExplainPageName: String?
)