package com.moegirlviewer.screen.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.ListWithMovableHeader
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.category.component.CategoryScreenItem
import com.moegirlviewer.screen.category.component.SubCategoryList
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.Italic
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.navigate
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CategoryScreen(
  arguments: CategoryRouteArguments
) {
  val model: CategoryScreenModal = hiltViewModel()
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors

  LaunchedEffect(true) {
    model.routeArguments = arguments

    if (model.statusOfPages == LoadStatus.INITIAL) {
      model.loadPages()
    }

    if (model.statusOfSubCategories == LoadStatus.INITIAL) {
      model.loadSubCategories()
    }
  }

  model.lazyListState.OnSwipeLoading {
    scope.launch { model.loadPages() }
  }

  Scaffold() {
    ListWithMovableHeader(
      maxDistance = 56.dp,
      lazyListState = model.lazyListState,
      header = {
        ComposedHeader(
          categoryName = arguments.categoryName,
          categories = if (arguments.parentCategories != null)
            arguments.parentCategories + listOf(arguments.categoryName) else
            emptyList()
        )
      }
    ) {
      LazyColumn(
        state = model.lazyListState
      ) {
        headerPlaceholder()

        if (arguments.categoryExplainPageName != null) {
          item {
            val annotatedString = buildAnnotatedString {
              append(stringResource(id = R.string.categoryNameMappedPage) + "：")
              pushStringAnnotation("link", "")
              withStyle(SpanStyle(
                color = themeColors.secondary,
                fontWeight = FontWeight.Bold
              )) {
                append(arguments.categoryExplainPageName)
              }
            }

            StyledText(
              modifier = Modifier
                .padding(top = 5.dp, start = 10.dp),
              style = TextStyle(
                color = themeColors.text.secondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
              ),
              text = annotatedString,
              onClick = { offset ->
                annotatedString.getStringAnnotations("link", offset, offset).firstOrNull()?.let {
                  Globals.navController.navigate(ArticleRouteArguments(
                    pageName = arguments.categoryExplainPageName
                  ))
                }
              }
            )
          }
        }

        item {
          AnimatedVisibility(
            visible = model.subCategories.isNotEmpty(),
            enter = expandVertically()
          ) {
            SubCategoryList()
          }
        }

        itemsIndexed(
          items = model.pages,
          key = { _, item -> item.pageid }
        ) { _, item ->
          CategoryScreenItem(
            pageName = item.title,
            thumbnail = item.thumbnail,
            // 貌似是mw的bug，有时获取到的分类下页面的数据，页面的所有分类居然是null
            categories = (item.categories ?: emptyList()).map { it.title.replaceFirst(Regex("^(分类|分類|Category):"), "") },
            onClick = {
              Globals.navController.navigate(ArticleRouteArguments(
                pageName = item.title
              ))
            },
            onCategoryClick = {
              Globals.navController.navigate(ArticleRouteArguments(
                pageName = "Category:$it",
                displayName = Globals.context.getString(R.string.category) + "：$it"
              ))
            }
          )
        }

        item {
          ScrollLoadListFooter(
            status = model.statusOfPages,
            emptyText = stringResource(id = R.string.emptyInCurrentCategory),
            onReload = {
              scope.launch { model.loadPages() }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun ComposedHeader(
  categoryName: String,
  categories: List<String>
) {
  val themeColors = MaterialTheme.colors
  val scrollState = rememberScrollState()

  LaunchedEffect(true) {
    scrollState.animateScrollTo(scrollState.maxValue)
  }

  Column() {
    StyledTopAppBar(
      navigationIcon = {
        BackButton()
      },
      title = {
        StyledText(
          text = "${stringResource(id = R.string.category)}：${categoryName}",
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = themeColors.onPrimary
        )
      },
    )

    if (categories.isNotEmpty()) {
      RippleColorScope(color = themeColors.onPrimary) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(themeColors.primary)
            .horizontalScroll(scrollState),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (item in categories) {
            Box(
              modifier = Modifier
                .clickable {
                  if (item != categories.last()) Globals.navController.navigate(
                    ArticleRouteArguments(
                      pageName = "Category:$item",
                      displayName = Globals.context.getString(R.string.category) + "：$item"
                    )
                  )
                }
                .fillMaxHeight()
                .padding(horizontal = 17.dp),
              contentAlignment = Alignment.Center
            ) {
              StyledText(
                text = item,
                color = themeColors.onPrimary
              )
            }

            if (item != categories.last()) {
              Box(
                modifier = Modifier
                  .offset(y = 1.dp)
              ) {
                Icon(
                  modifier = Modifier
                    .size(30.dp),
                  imageVector = Icons.Filled.ChevronRight,
                  contentDescription = null,
                  tint = themeColors.onPrimary
                )
              }
            }
          }
        }
      }
    }
  }
}