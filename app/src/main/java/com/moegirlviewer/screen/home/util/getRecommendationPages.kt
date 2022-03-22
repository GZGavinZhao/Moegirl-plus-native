package com.moegirlviewer.screen.home.util

import com.moegirlviewer.api.category.CategoryApi
import com.moegirlviewer.api.category.CategoryApiPagesSort
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.categoryPageNamePrefixRegex
import com.moegirlviewer.util.randomList
import kotlinx.coroutines.flow.first
import kotlin.math.round

suspend fun getRecommendationPages(
  count: Int = 5
): RecommendationPagesResult? {
  val browsingRecordList = Globals.room.browsingRecord().getAll().first()
  if (browsingRecordList.isEmpty()) return null

  // 如果浏览记录大于5个，则随机抽出5个页面
  val randomPagesFromBrowsingRecordList = browsingRecordList.map { it.pageName }.randomList(5).reversed()

  // 取出全部页面的全部分类，并展平，其中有重复的
  val pageCategories = CategoryApi.getPageCategories(randomPagesFromBrowsingRecordList)
    .query.pages.values
    .filter { it.categories != null }
  val allCategories = pageCategories
    .map { it.categories!!.map { it.title } }
    .flatten()

  if (allCategories.isEmpty()) return null

  class CountedCategory(
    val name: String,
    var count: Int = 1
  )

  // 将重复分类的合并，并按重复次数从小到大排序
  val countedCategories = allCategories.fold(mutableListOf<CountedCategory>()) { result, item ->
    val existsItem = result.firstOrNull { it.name == item }
    if (existsItem != null) existsItem.count++ else result.add(CountedCategory(item))
    result
  }
    .apply { sortBy { it.count } }

  // 取分类列表里中间的分类
  // 为什么不取最大的：最大的一般为“人物”、“声优”这种较广的分类，无法正确反映用户偏好
  val medianCategoryFullName = countedCategories.run {
    val medianIndex = round(((this.size - 1) / 2).toFloat()).toInt()
    this[medianIndex].name
  }
  val medianCategoryName = medianCategoryFullName.replaceFirst(categoryPageNamePrefixRegex, "")
  val pagesOfMaximalCategory = CategoryApi.search(
    categoryName = medianCategoryName,
    sort = CategoryApiPagesSort.values().random(),
    limit = 500,
    thumbSize = 1,
  )
    .query?.pages?.values?.map { it.title } ?: emptyList()

  val randomPagesOfMaximalCategory = pagesOfMaximalCategory.randomList(5)
  val randomPagesWithMainImage = PageApi.getMainImageAndIntroduction(
    *randomPagesOfMaximalCategory.toTypedArray(),
    size = Globals.activity.resources.displayMetrics.widthPixels
  )
    .query.pages.values
    .map {
      RecommendationPage(
        pageName = it.title,
        imageUrl = it.thumbnail?.source,
        introduction = it.extract
      )
    }

  return RecommendationPagesResult(
    sourceCategoryName = medianCategoryName,
    sourcePageName = pageCategories.first {
      it.categories!!.any { it.title == medianCategoryFullName }
    }.title,
    body = randomPagesWithMainImage
  )
}

class RecommendationPagesResult(
  val sourcePageName: String,
  val sourceCategoryName: String,
  val body: List<RecommendationPage>
)

class RecommendationPage(
  val pageName: String,
  val imageUrl: String?,
  val introduction: String
)