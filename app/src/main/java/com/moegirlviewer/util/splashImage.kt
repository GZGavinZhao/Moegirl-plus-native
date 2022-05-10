package com.moegirlviewer.util

import android.graphics.drawable.Drawable
import com.moegirlviewer.R

open class SplashImage(
  val imageData: Any,
  val title: String,
  val author: String,
) {
  companion object {
    fun onlyUseInSplashScreen(
      imageData: Any
    ) = SplashImage(
      imageData = imageData,
      title = "",
      author = ""
    )
  }
}

class MoegirlSplashImage(
  imageData: Drawable,
  title: String,
  author: String,
  val key: String,
  val season: String,
) : SplashImage(
  imageData = imageData,
  title = title,
  author = author
)
