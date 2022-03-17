package com.moegirlviewer.compable.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

@Composable
fun rememberImageRequest(
  data: Any?,
  builder: (ImageRequest.Builder.() -> Unit)? = null
): ImageRequest {
  val context = LocalContext.current
  return remember(data) {
    ImageRequest.Builder(context)
      .data(data)
      .crossfade(true)
      .apply { builder?.invoke(this) }
      .build()
  }
}