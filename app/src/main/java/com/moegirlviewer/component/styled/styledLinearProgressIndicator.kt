package com.moegirlviewer.component.styled

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StyleLinearProgressIndicator(
  modifier: Modifier = Modifier
) {
  val themeColors = MaterialTheme.colors

  LinearProgressIndicator(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    color = themeColors.primaryVariant
  )
}