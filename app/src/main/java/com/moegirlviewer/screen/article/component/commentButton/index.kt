package com.moegirlviewer.screen.article.component.commentButton

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.comment.CommentRouteArguments
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun BoxScope.CommentButton(
  pageId: Int,
  pageName: String,
  visible: Boolean
) {
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()
  val currentPageComments by CommentStore.rememberGetCommentsStateById(pageId)
  val buttonText = when(currentPageComments.status) {
    LoadStatus.FAIL -> "×"
    LoadStatus.SUCCESS,
    LoadStatus.ALL_LOADED,
    LoadStatus.EMPTY -> currentPageComments.count.toString()
    else -> "..."
  }

  fun handleOnClickButton() = scope.launch {
    when (currentPageComments.status) {
      LoadStatus.FAIL -> CommentStore.loadNext(pageId)
      LoadStatus.LOADING,
      LoadStatus.INIT_LOADING -> toast(Globals.context.getString(R.string.loading))
      else -> {
        Globals.navController.navigate(CommentRouteArguments(
          pageId = pageId,
          pageName = pageName
        ))
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .absoluteOffset((-20).dp, (-20).dp),
    contentAlignment = Alignment.BottomEnd
  ) {
    AnimatedVisibility(
      visible = visible,
      enter = slideInVertically(
        initialOffsetY = { density.run { 100.dp.roundToPx() } }
      ),
      exit = slideOutVertically(
        targetOffsetY = { density.run { 100.dp.roundToPx() } }
      )
    ) {
      ButtonBody(
        text = buttonText,
        onClick = { handleOnClickButton() }
      )
    }
  }
}

@Composable
private fun ButtonBody(
  text: String,
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors

  Surface(
    modifier = Modifier
      .size(60.dp)
      .noRippleClickable { onClick() },
    shape = CircleShape,
    elevation = 3.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(if (themeColors.isLight) themeColors.secondary else themeColors.primary),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .offset(0.dp, (-5).dp)
      ) {
        Icon(
          modifier = Modifier
            .size(28.dp),
          imageVector = Icons.Filled.Comment,
          contentDescription = null,
          tint = if (themeColors.isLight) themeColors.onSecondary else themeColors.onPrimary
        )
      }

      Box(
        modifier = Modifier
          .offset(0.dp, 15.dp)
      ) {
        StyledText(
          text = text,
          color = if (themeColors.isLight) themeColors.onSecondary else themeColors.onPrimary,
          fontSize = 13.sp,
        )
      }
    }
  }
}