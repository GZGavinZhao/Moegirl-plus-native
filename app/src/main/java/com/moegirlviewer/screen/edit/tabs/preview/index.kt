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
import com.moegirlviewer.component.articleView.ArticleCatalog
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.articleView.ArticleViewRef
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.screen.article.component.catalog.ArticleScreenCatalog
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.util.LoadStatus
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
  val articleViewRef = remember { Ref<ArticleViewRef>() }

  LaunchedEffect(model.previewHtml) {
    articleViewRef.value?.updateView?.invoke()
  }

  Center {
    ArticleScreenCatalog(
      statusBarPadding = false,
      catalogData = catalogData,
      onSectionClick = {
        scope.launch {
          articleViewRef.value!!.htmlWebViewRef!!.injectScript(
            "moegirl.method.link.gotoAnchor('$it', -10)"
          )
        }
      }
    ) {
      ArticleView(props = ArticleViewProps(
        html = model.previewHtml,
        pageName = model.routeArguments.pageName,
        linkDisabled = true,
        addCopyright = false,
        emitCatalogData = { catalogData = it },
        ref = articleViewRef
      ))
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