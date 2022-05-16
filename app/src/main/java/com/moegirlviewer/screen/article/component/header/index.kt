package com.moegirlviewer.screen.article.component.header

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.compable.CachedStatusBarConfig
import com.moegirlviewer.compable.StatusBarMode
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.article.ArticleScreenModel
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.replace

@Suppress("UpdateTransitionLabel", "TransitionPropertiesLabel")
@Composable
fun ArticleScreenHeader(
  modifier: Modifier = Modifier,
  title: String,
  visible: Boolean,
  deepLinkMode: Boolean,
  onAction: (action: MoreMenuAction) -> Unit,
) {
  val themeColors = MaterialTheme.colors
  val transition = updateTransition(visible)
  var visibleMoreMenu by remember { mutableStateOf(false) }
  val isFocusMode by SettingsStore.common.getValue { focusMode }.collectAsState(initial = null)
  if (isFocusMode == null) return
  
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
      statusBarMode = if (isFocusMode!!) StatusBarMode.STICKY else StatusBarMode.VISIBLE,
      statusBarSticky = isFocusMode!!,
      modifier = Modifier
        .then(modifier)
        .absoluteOffset(0.dp, topOffset)
      ,
      navigationIcon = {
        if (deepLinkMode) {
          AppHeaderIcon(
            modifier = Modifier
              .alpha(contentAlpha),
            image = Icons.Filled.Home,
            onClick = {
              Globals.navController.replace("home")
            }
          )
        } else {
          BackButton(
            modifier = Modifier
              .alpha(contentAlpha)
          )
        }
      },
      title = {
        StyledText(
          modifier = Modifier
            .alpha(contentAlpha),
          text = title,
          overflow = TextOverflow.Ellipsis,
          maxLines = 2,
          fontSize = 18.sp,
          color = themeColors.onPrimary
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

        Box() {
          AppHeaderIcon(
            modifier = Modifier
              .alpha(contentAlpha),
            image = Icons.Filled.MoreVert,
            onClick = {
              visibleMoreMenu = true
            }
          )

          Box(
            modifier = Modifier
              .offset(x = 52.dp, y = 52.dp)
          ) {
            MoreMenu(
              visible = visibleMoreMenu,
              onAction = {
                onAction(it)
                visibleMoreMenu = false
              },
              onDismiss = { visibleMoreMenu = false }
            )
          }
        }
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
  val themeColors = MaterialTheme.colors
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)

  @Composable
  fun MoreMenuItem(
    action: MoreMenuAction,
    text: String,
    enabled: Boolean = true,
  ) {
    DropdownMenuItem(
      enabled = enabled,
      onClick = { onAction(action) }
    ) {
      StyledText(
        text = text,
        color = if (enabled) themeColors.text.primary else themeColors.text.tertiary
      )
    }
  }

  DropdownMenu(
    expanded = visible,
    onDismissRequest = onDismiss
  ) {
    MoreMenuItem(
      action = MoreMenuAction.REFRESH,
      text = stringResource(id = R.string.refresh)
    )

    if (isLoggedIn) {
      MoreMenuItem(
        enabled = model.editAllowed.allowed,
        action = if (model.editAllowed == EditAllowedStatus.ALLOWED_SECTION)
          MoreMenuAction.GOTO_ADD_SECTION else
          MoreMenuAction.GOTO_EDIT
        ,
        text = stringResource(
          id = when(model.editAllowed) {
            EditAllowedStatus.CHECKING -> R.string.permissionsChecking
            EditAllowedStatus.DISABLED -> R.string.noAllowEditThePage
            EditAllowedStatus.ALLOWED_SECTION -> R.string.addTopic
            EditAllowedStatus.ALLOWED_FULL -> R.string.editThePage
          }
        )
      )
    } else {
      MoreMenuItem(
        action = MoreMenuAction.GOTO_LOGIN,
        text = stringResource(id = R.string.loginToEdit)
      )
    }

    if (isLoggedIn) {
      MoreMenuItem(
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
      MoreMenuItem(
        action = MoreMenuAction.GOTO_TALK,
        text = stringResource(id = R.string.talk)
      )
    }

    MoreMenuItem(
      action = MoreMenuAction.GOTO_PAGE_REVISIONS,
      text = stringResource(id = R.string.pageVersionHistory)
    )
    MoreMenuItem(
      action = MoreMenuAction.SHARE,
      text = stringResource(id = R.string.share)
    )
    MoreMenuItem(
      action = MoreMenuAction.SHOW_FIND_BAR,
      text = stringResource(id = R.string.findInPage)
    )
    MoreMenuItem(
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

enum class EditAllowedStatus(val allowed: Boolean = false) {
  ALLOWED_SECTION(true),
  ALLOWED_FULL(true),
  DISABLED,
  CHECKING
}