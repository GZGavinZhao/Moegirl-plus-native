package com.moegirlviewer.screen.article.component.header

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.article.ArticleScreenModel
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals

@Suppress("UpdateTransitionLabel", "TransitionPropertiesLabel")
@Composable
fun ArticleScreenHeader(
  modifier: Modifier = Modifier,
  title: String,
  visible: Boolean,
  onAction: (action: MoreMenuAction) -> Unit,
) {
  val transition = updateTransition(visible)
  var visibleMoreMenu by remember { mutableStateOf(false) }
  
  val offsetTopForHidden = -Constants.topAppBarHeight

  fun <T> animation() = tween<T>(
    durationMillis = 300,
    easing = LinearEasing
  )

  val topOffset by transition.animateDp({ animation() }) {
    if (it) 0.dp else offsetTopForHidden.dp
  }

  val contentAlpha by transition.animateFloat({ animation() }) {
    if (it) 1f else 0f
  }

  Box() {
    StyledTopAppBar(
      modifier = Modifier
        .then(modifier)
        .absoluteOffset(0.dp, topOffset)
      ,
      navigationIcon = {
        BackButton(
          modifier = Modifier
            .alpha(contentAlpha)
        )
      },
      title = {
        Text(
          modifier = Modifier
            .alpha(contentAlpha),
          text = title,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
      },
      actions = {
        AppHeaderIcon(
          modifier = Modifier
            .alpha(contentAlpha),
          image = Icons.Filled.Search,
          onClick = {
            Globals.navController.navigate("search")
          }
        )

        AppHeaderIcon(
          modifier = Modifier
            .alpha(contentAlpha),
          image = Icons.Filled.MoreVert,
          onClick = {
            visibleMoreMenu = true
          }
        )

        MoreMenu(
          visible = visibleMoreMenu,
          onAction = {
            onAction(it)
            visibleMoreMenu = false
          },
          onDismiss = { visibleMoreMenu = false }
        )
      }
    )
  }
}

@Composable
private fun MoreMenu(
  visible: Boolean,
  onAction: (action: MoreMenuAction) -> Unit,
  onDismiss: () -> Unit,
) {
  val model: ArticleScreenModel = hiltViewModel()
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)

  @Composable
  fun Item(
    action: MoreMenuAction,
    text: String,
    enabled: Boolean = true,
  ) {
    DropdownMenuItem(
      enabled = enabled,
      onClick = { onAction(action) }
    ) {
      Text(text)
    }
  }

  DropdownMenu(
    expanded = visible,
    onDismissRequest = onDismiss
  ) {
    Item(
      action = MoreMenuAction.REFRESH,
      text = stringResource(id = R.string.refresh)
    )

    if (isLoggedIn) {
      Item(
        enabled = model.editAllowed ?: false,
        action = if (model.editFullDisabled)
          MoreMenuAction.REFRESH else
          MoreMenuAction.GOTO_EDIT
        ,
        text = stringResource(
          id = when {
            model.editAllowed == null -> R.string.permissionsChecking
            model.editAllowed == false -> R.string.noAllowEditThePage
            model.editFullDisabled -> R.string.addTopic
            else -> R.string.editThePage
          }
        )
      )
    } else {
      Item(
        action = MoreMenuAction.GOTO_LOGIN,
        text = stringResource(id = R.string.login)
      )
    }

    if (isLoggedIn) {
      Item(
        action = MoreMenuAction.TOGGLE_WATCH_LIST,
        text = stringResource(id = R.string.operateWatchList,
          stringResource(id = if (model.isWatched)
            R.string.remove else
            R.string.join
          )
        )
      )
    }

    if (model.visibleTalkButton) {
      Item(
        action = MoreMenuAction.GOTO_TALK,
        text = stringResource(id = R.string.talk)
      )
    }

    Item(
      action = MoreMenuAction.GOTO_PAGE_REVISIONS,
      text = stringResource(id = R.string.pageVersionHistory)
    )
    Item(
      action = MoreMenuAction.SHARE,
      text = stringResource(id = R.string.share)
    )
    Item(
      action = MoreMenuAction.SHOW_FIND_BAR,
      text = stringResource(id = R.string.findInPage)
    )
    Item(
      action = MoreMenuAction.SHOW_CATALOG,
      text = stringResource(id = R.string.showCatalog)
    )
  }
}

enum class MoreMenuAction {
  REFRESH,
  GOTO_EDIT,
  GOTO_LOGIN,
  TOGGLE_WATCH_LIST,
  SHOW_CATALOG,
  SHARE,
  GOTO_ADD_SECTION,
  GOTO_TALK,
  GOTO_PAGE_REVISIONS,
  SHOW_FIND_BAR
}