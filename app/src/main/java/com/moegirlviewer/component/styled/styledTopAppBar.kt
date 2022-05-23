package com.moegirlviewer.component.styled

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.compable.StatusBarMode
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.theme.elevation
import com.moegirlviewer.theme.isUsePureTheme
import com.moegirlviewer.util.Globals
import kotlinx.coroutines.delay

@Composable
fun StyledTopAppBar(
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colors.primary,
  contentColor: Color = contentColorFor(backgroundColor),
  elevation: Dp = if (MaterialTheme.elevation) 3.dp else 0.dp,
  statusBarMode: StatusBarMode = StatusBarMode.VISIBLE,
  statusBarSticky: Boolean = false,
  statusBarBackgroundColor: Color = backgroundColor,
  statusBarDarkIcons: Boolean = isUsePureTheme() || isSystemInDarkTheme(),
  title: @Composable () -> Unit,
  navigationIcon: (@Composable () -> Unit) = { BackButton() },
  actions: (@Composable (RowScope.() -> Unit))? = null,
) {
  val themeColor = MaterialTheme.colors

  StatusBar(
    mode = statusBarMode,
    sticky = statusBarSticky,
    backgroundColor = Color.Transparent,
    darkIcons = statusBarDarkIcons
  )

  Box {
    if (statusBarMode == StatusBarMode.VISIBLE) {
      Spacer(modifier = modifier
        .fillMaxWidth()
        .background(statusBarBackgroundColor)
        .padding(top = Globals.statusBarHeight.dp)
        .absoluteOffset(0.dp, 0.dp)
        .zIndex(1f)
      )
    }

    TopAppBar(
      modifier = Modifier
        .padding(top = (if (statusBarMode == StatusBarMode.VISIBLE) Globals.statusBarHeight else 0f).dp)
        .then(modifier),
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      elevation = elevation,
      title = {
        CompositionLocalProvider(
          LocalTextStyle provides LocalTextStyle.current.copy(
            color = themeColor.onPrimary,
            fontWeight = FontWeight.Bold,
          ),
          content = title
        )
      },
      navigationIcon = {
        CompositionLocalProvider(
          LocalContentAlpha provides ContentAlpha.medium,
          content = navigationIcon
        )
      },
      actions = {
        actions?.invoke(this)
      },
    )
  }
}