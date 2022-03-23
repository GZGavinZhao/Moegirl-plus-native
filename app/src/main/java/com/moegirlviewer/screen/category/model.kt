package com.moegirlviewer.screen.category

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.moegirlviewer.api.category.CategoryApi
import com.moegirlviewer.api.category.CategoryApiPagesSort
import com.moegirlviewer.api.category.bean.CategorySearchResultBean
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.categoryPageNamePrefixRegex
import com.moegirlviewer.util.printRequestErr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@HiltViewModel
class CategoryScreenModal @Inject constructor() : ViewModel() {
  lateinit var routeArguments: CategoryRouteArguments
  var pages by mutableStateOf(emptyList<CategorySearchResultBean.Query.MapValue>())
  var statusOfPages by mutableStateOf(LoadStatus.INITIAL)
  var continueKeyOfPages: CategorySearchResultBean.Continue? = null
  val lazyListState = LazyListState()
  var categorySort by mutableStateOf(CategoryApiPagesSort.NEWER)

  var subCategories by mutableStateOf(emptyList<String>())
  var statusOfSubCategories by mutableStateOf(LoadStatus.INITIAL)
  var continueKeyOfSubCategories: String? = null

  suspend fun loadPages(
    reload: Boolean = false
  ) {
    if (reload) {
      statusOfPages = LoadStatus.INITIAL
      continueKeyOfPages = null
      pages = emptyList()
    }

    if (LoadStatus.isCannotLoad(statusOfPages)) return
    statusOfPages = LoadStatus.LOADING

    try {
      val res = CategoryApi.search(
        categoryName = routeArguments.categoryName,
        thumbSize = 360,
        continueKey =  continueKeyOfPages,
        sort = categorySort
      )

      if (res.query == null) {
        statusOfPages = LoadStatus.EMPTY
        return
      }

      val resList = res.query.pages.values.toList()

      val nextStatus = when {
        pages.isEmpty() && res.query.pages.isEmpty() -> LoadStatus.EMPTY
        res.`continue`?.gcmcontinue == null && resList.isNotEmpty() -> LoadStatus.ALL_LOADED
        else -> LoadStatus.SUCCESS
      }

      pages = pages + resList
      statusOfPages = nextStatus
      continueKeyOfPages = res.`continue`
    } catch (e: MoeRequestException) {
      printRequestErr(e, "获取分类下页面列表失败")
      statusOfPages = LoadStatus.FAIL
    }
  }

  suspend fun loadSubCategories() {
    if (LoadStatus.isCannotLoad(statusOfSubCategories)) return
    statusOfSubCategories = LoadStatus.LOADING

    try {
      val res = CategoryApi.getSubCategories(
        categoryName = routeArguments.categoryName,
        continueKey = continueKeyOfSubCategories
      )

      val categories = res.query.categorymembers.map {
        it.title.replaceFirst(categoryPageNamePrefixRegex, "")
      }

      val continueKey = res.`continue`?.cmcontinue

      val nextStatus = when {
        categories.isEmpty() -> LoadStatus.EMPTY
        categories.isNotEmpty() && continueKey == null -> LoadStatus.ALL_LOADED
        else -> LoadStatus.SUCCESS
      }

      subCategories = subCategories + categories
      continueKeyOfSubCategories = continueKey
      statusOfSubCategories = nextStatus
    } catch (e: MoeRequestException) {
      printRequestErr(e, "加载子分类失败")
      statusOfSubCategories = LoadStatus.FAIL
    }
  }

  override fun onCleared() {
    super.onCleared()

    routeArguments.removeReferencesFromArgumentPool()
  }
}