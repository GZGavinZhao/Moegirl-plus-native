package com.moegirlviewer.screen.category.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lens
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.category.CategoryScreenModal
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun SubCategoryList() {
  val model: CategoryScreenModal = hiltViewModel()
  val scope = rememberCoroutineScope()
  var isExpanded by rememberSaveable { mutableStateOf(false) }

  val themeColors = MaterialTheme.colors

  Surface(
    modifier = Modifier
      .padding(15.dp)
      .fillMaxWidth(),
    elevation = 2.dp
  ) {
    Column() {
      // header
      Row(
        modifier = Modifier
          .clickable { isExpanded = !isExpanded }
          .height(40.dp)
          .fillMaxWidth()
          .padding(horizontal = 10.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        StyledText(
          text = stringResource(id = R.string.subCategoryList),
          color = themeColors.text.secondary
        )

        Icon(
          modifier = Modifier
            .size(20.dp),
          imageVector = if (isExpanded)
            Icons.Filled.ExpandLess else
            Icons.Filled.ExpandMore,
          contentDescription = null
        )
      }

      // list
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
          expandFrom = Alignment.Top
        ),
        exit = shrinkVertically(
          shrinkTowards = Alignment.Top
        )
      ) {
        Column(
          modifier = Modifier
            .sideBorder(BorderSide.TOP, 1.dp, themeColors.text.tertiary)
            .fillMaxWidth()
            .padding(vertical = 7.dp, horizontal = 10.dp)
        ) {
          for (item in model.subCategories) {
            Row(
              modifier = Modifier
                .padding(bottom = 2.5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                modifier = Modifier
                  .size(8.dp),
                imageVector = Icons.Filled.Lens,
                contentDescription = null,
                tint = themeColors.text.tertiary
              )
              StyledText(
                modifier = Modifier
                  .padding(start = 5.dp)
                  .noRippleClickable {
                    Globals.navController.navigate(ArticleRouteArguments(
                      pageName = "Category:$item",
                      displayName = Globals.context.getString(R.string.category) + "：$item"
                    ))
                  },
                text = item,
                fontSize = 15.5.sp,
                color = themeColors.primaryVariant
              )
            }
          }

          if (model.statusOfSubCategories == LoadStatus.SUCCESS) {
            Box(
              modifier = Modifier
                .noRippleClickable {
                  scope.launch { model.loadSubCategories() }
                }
                .fillMaxWidth()
                .padding(vertical = 10.dp)
              ,
              contentAlignment = Alignment.Center
            ) {
              StyledText(
                text = stringResource(id = R.string.loadMore),
                color = themeColors.primaryVariant,
                fontSize = 15.sp
              )
            }
          }

          if (model.statusOfSubCategories == LoadStatus.LOADING) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              StyledCircularProgressIndicator()
            }
          }
        }
      }
    }
  }
}