package com.moegirlviewer.api.app.bean

data class HmoeSplashImageConfigBean(
  val festivals: List<Festival>,
  val images: List<Image>
) {
  data class Festival(
    val date: String,
    val imageUrls: List<String>,
    val disabled: Boolean = false
  )

  data class Image(
    val imageUrl: String,
    val title: String,
    val disabled: Boolean = false
  )
}