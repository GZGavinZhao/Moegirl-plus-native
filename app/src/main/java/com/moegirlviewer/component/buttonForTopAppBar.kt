package com.moegirlviewer.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moegirlviewer.util.Globals

@Composable
fun TopAppBarIcon(
  modifier: Modifier = Modifier,
  image: ImageVector,
  iconSize: Dp = 29.dp,
  iconColor: Color = LocalContentColor.current,
  onClick: () -> Unit,
) {
  IconButton(
    onClick = onClick
  ) {
    Icon(
      modifier = Modifier
        .width(iconSize)
        .height(iconSize)
        .then(modifier),
      imageVector = image,
      contentDescription = null,
      tint = iconColor,
    )
  }
}

@Composable
fun BackButton(
  modifier: Modifier = Modifier,
  iconColor: Color = LocalContentColor.current,
) {
  TopAppBarIcon(
    modifier = Modifier
      .then(modifier),
    image = Icons.Filled.ArrowBack,
    iconSize = 29.dp,
    iconColor = iconColor,
    onClick = { Globals.navController.popBackStack() }
  )
}