package com.moegirlviewer.api.category

import com.moegirlviewer.api.category.bean.CategorySearchResultBean
import com.moegirlviewer.api.category.bean.PageCategoriesBean
import com.moegirlviewer.api.category.bean.SubCategoriesBean
import com.moegirlviewer.request.moeRequest

object CategoryApi {
  suspend fun search(
    categoryName: String,
    thumbSize: Int,
    sort: CategoryApiPagesSort = CategoryApiPagesSort.DESCENDING,
    limit: Int = 50,
    continueKey: CategorySearchResultBean.Continue? = null
  ) = moeRequest(
    entity = CategorySearchResultBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["prop"] = "pageimages|categories"
      this["cllimit"] = 500
      this["generator"] = "categorymembers"
      this["pilimit"] = "50"
      this["gcmtitle"] = "Category:$categoryName"
      this["gcmprop"] = "sortkey|sortkeyprefix"
      this["gcmnamespace"] = "0"
      this["gcmlimit"] = limit
      this["pithumbsize"] = thumbSize
      this["clshow"] = "!hidden"
      this["gcmdir"] = sort.sortDir
      this["gcmsort"] = sort.sortMethod
      if (continueKey != null) {
        if (continueKey.`continue` != null) this["continue"] = continueKey.`continue`
        if (continueKey.gcmcontinue != null) this["gcmcontinue"] = continueKey.gcmcontinue
      }
    }
  )

  suspend fun getSubCategories(
    categoryName: String,
    continueKey: String?
  ) = moeRequest(
    entity = SubCategoriesBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["list"] = "categorymembers"
      this["cmtitle"] = "Category:$categoryName"
      this["cmprop"] = "title"
      this["cmtype"] = "subcat"
      this["cmlimit"] = 100
      this["continue"] = "-||"
      if (continueKey != null) this["cmcontinue"] = continueKey
    }
  )

  suspend fun getPageCategories(
    pages: List<String>,
  ) = moeRequest(
    entity = PageCategoriesBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["prop"] = "categories"
      this["titles"] = pages.joinToString("|")
      this["clshow"] = "!hidden"
      this["cllimit"] = 500
    }
  )
}

enum class CategoryApiPagesSort(
  val sortMethod: String,
  val sortDir: String
) {
  ASCENDING("sortkey", "ascending"),
  DESCENDING("sortkey", "descending"),
  NEWER("timestamp", "newer"),
  OLDER("timestamp", "older")
}