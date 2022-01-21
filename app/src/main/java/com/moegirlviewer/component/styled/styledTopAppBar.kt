package com.moegirlviewer.component.styled

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.moegirlviewer.compable.StatusBar
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.extants.toCssRgbaString
import com.moegirlviewer.util.Globals

@Composable
fun StyledTopAppBar(
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colors.primary,
  contentColor: Color = contentColorFor(backgroundColor),
  elevation: Dp = 0.dp,
  statusBarBackgroundColor: Color = backgroundColor,
  statusBarDarkIcons: Boolean = false,
  title: @Composable () -> Unit,
  navigationIcon: (@Composable () -> Unit) = { BackButton() },
  actions: (@Composable (RowScope.() -> Unit))? = null,
) {
  val themeColor = MaterialTheme.colors

  StatusBar(
    backgroundColor = Color.Transparent,
    darkIcons = statusBarDarkIcons
  )

  Box {
    Spacer(modifier = modifier
      .fillMaxWidth()
      .background(statusBarBackgroundColor)
      .statusBarsPadding()
      .absoluteOffset(0.dp, 0.dp)
      .zIndex(1f)
    )

    TopAppBar(
      modifier = Modifier
        .statusBarsPadding()
        .then(modifier),
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      elevation = elevation,
      title = title,
      navigationIcon = {
        CompositionLocalProvider(
          LocalContentAlpha provides ContentAlpha.medium
        ) {
          navigationIcon()
        }
      },
      actions = {
        actions?.invoke(this)
      },
    )
  }
}