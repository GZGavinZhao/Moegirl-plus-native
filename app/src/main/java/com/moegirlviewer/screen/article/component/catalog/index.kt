package com.moegirlviewer.screen.article.component.catalog

import com.moegirlviewer.component.articleView.ArticleCatalog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.customDrawer.CustomDrawer
import com.moegirlviewer.component.customDrawer.CustomDrawerSide
import com.moegirlviewer.component.customDrawer.CustomDrawerState
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.sideBorder
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun ArticleScreenCatalog(
  catalogData: List<ArticleCatalog>,
  statusBarPadding: Boolean = true,
  onSectionClick: (sectionId: String) -> Unit,
  customDrawerState: CustomDrawerState = remember { CustomDrawerState() },
  content: @Composable () -> Unit
) {
  val themeColors = MaterialTheme.colors

  CustomDrawer(
    state = customDrawerState,
    width = (LocalConfiguration.current.screenWidthDp * 0.65).dp,
    side = CustomDrawerSide.RIGHT,
    alwaysDelayInitialize = true,
    drawerContent = {
      Column(
        modifier = Modifier
          .fillMaxSize()
      ) {
        ComposedHeader(
          statusBarPadding = statusBarPadding,
          customDrawerState = customDrawerState
        )

        RippleColorScope(color = themeColors.primaryVariant) {
          Column(
            modifier = Modifier
              .padding(vertical = 5.dp, horizontal = 10.dp)
              .weight(1f)
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
          ) {
            for (item in catalogData) {
              SectionItem(
                name = item.name,
                level = item.level,
                onClick = { onSectionClick(item.id) }
              )
            }
          }
        }
      }
    }
  ) {
    content()
  }
}

@Composable
private fun ComposedHeader(
  statusBarPadding: Boolean,
  customDrawerState: CustomDrawerState
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  Row(
    modifier = Modifier
      .sideBorder(
        side = BorderSide.TOP,
        width = if (statusBarPadding) Globals.statusBarHeight.dp else 0.dp,
        color = themeColors.primary
      )
      .fillMaxWidth()
      .height(Constants.topAppBarHeight.dp)
      .background(themeColors.primary)
      .padding(horizontal = 10.dp)
    ,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    StyledText(
      text = stringResource(id = R.string.contents),
      color = themeColors.onPrimary,
      fontSize = 20.sp
    )

    RippleColorScope(color = themeColors.onPrimary) {
      IconButton(
        onClick = {
          scope.launch { customDrawerState.close() }
        }
      ) {
        Icon(
          modifier = Modifier
            .size(40.dp),
          imageVector = Icons.Filled.ChevronRight,
          contentDescription = null,
          tint = themeColors.onPrimary
        )
      }
    }
  }
}

@Composable
private fun SectionItem(
  name: String,
  level: Int,
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors
  val paddingLeft = (if (level - 2 < 0) 0 else level - 2) * 10

  Box(
    modifier = Modifier
      .height(30.dp)
      .fillMaxWidth()
      .clickable { onClick() },
    contentAlignment = Alignment.CenterStart,
  ) {
    StyledText(
      modifier = Modifier
        .padding(start = 3.dp + paddingLeft.dp),
      text = (if (level >= 3) "- " else "") + name,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      fontSize = if (level < 3) 18.sp else 16.sp,
      color = if (level < 3) themeColors.primaryVariant else themeColors.text.secondary
    )
  }
}