package com.moegirlviewer.component.styled

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moegirlviewer.ui.theme.text

@Composable
fun StyledCircularProgressIndicator(
  modifier: Modifier = Modifier
) {
  val themeColors = MaterialTheme.colors

  CircularProgressIndicator(
    modifier = Modifier,
    color = themeColors.secondary
  )
}