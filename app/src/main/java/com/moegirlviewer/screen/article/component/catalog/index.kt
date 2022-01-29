package com.moegirlviewer.screen.article.component.catalog

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.articleView.ArticleCatalog
import com.moegirlviewer.component.customDrawer.CustomDrawer
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.customDrawer.CustomDrawerSide
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.sideBorder

@ExperimentalMaterialApi
@Composable
fun ArticleScreenCatalog(
  catalogData: List<ArticleCatalog>,
  statusBarPadding: Boolean = true,
  onSectionClick: (sectionId: String) -> Unit,
  ref: Ref<CustomDrawerRef>? = null,
  content: @Composable () -> Unit
) {
  val themeColors = MaterialTheme.colors

  CustomDrawer(
    width = (LocalConfiguration.current.screenWidthDp * 0.55).dp,
    side = CustomDrawerSide.RIGHT,
    alwaysDelayInitialize = true,
    ref = ref,
    drawerContent = {
      Column(
        modifier = Modifier
          .fillMaxSize()
      ) {
        ComposedHeader(
          statusBarPadding = statusBarPadding,
          drawerRef = it
        )

        RippleColorScope(color = themeColors.secondary) {
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
  drawerRef: CustomDrawerRef
) {
  val themeColors = MaterialTheme.colors

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
          drawerRef.close()
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
      fontSize = if (level < 3) 16.sp else 14.sp,
      color = if (level < 3) themeColors.secondary else themeColors.text.secondary
    )
  }
}