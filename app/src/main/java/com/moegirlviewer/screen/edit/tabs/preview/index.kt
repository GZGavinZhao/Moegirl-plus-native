package com.moegirlviewer.screen.edit.tabs.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.articleView.*
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.screen.article.component.catalog.ArticleScreenCatalog
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.PageNameKey
import com.moegirlviewer.util.noRippleClickable
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun EditScreenPreview() {
  val model: EditScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  var catalogData by rememberFromMemory("catalogData") {
    mutableStateOf(emptyList<ArticleCatalog>())
  }
  val articleViewState = remember { ArticleViewState() }

  LaunchedEffect(model.previewHtml) {
    articleViewState.updateHtmlView(true)
  }

  Center {
    ArticleScreenCatalog(
      statusBarPadding = false,
      catalogData = catalogData,
      onSectionClick = {
        scope.launch {
          articleViewState.injectScript(
            "moegirl.method.link.gotoAnchor('$it', -10)"
          )
        }
      }
    ) {
      ArticleView(
        state = articleViewState,
        html = model.previewHtml,
        pageKey = PageNameKey(model.routeArguments.pageName),
        linkDisabled = true,
        addCopyright = false,
        addCategories = model.routeArguments.section == null,
        visibleLoadStatusIndicator = false,
        emitCatalogData = { catalogData = it }
      )
    }

    if (model.previewStatus != LoadStatus.SUCCESS) {
      Center(
        modifier = Modifier
          .noRippleClickable {  }
          .absoluteOffset(0.dp, 0.dp)
          .background(themeColors.background)
      ) {
        if (model.previewStatus == LoadStatus.FAIL) {
          ReloadButton(
            modifier = Modifier
              .matchParentSize(),
            onClick = {
              scope.launch { model.loadPreview() }
            }
          )
        } else {
          StyledCircularProgressIndicator()
        }
      }
    }
  }
}