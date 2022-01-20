package com.moegirlviewer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Center(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}