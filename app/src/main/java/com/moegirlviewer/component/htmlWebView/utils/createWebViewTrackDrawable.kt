package com.moegirlviewer.component.htmlWebView.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.unit.Density
import com.moegirlviewer.util.Globals
import kotlin.math.roundToInt

fun createWebViewTrackDrawable(density: Density): BitmapDrawable {
  val bitmap = Bitmap.createBitmap(
    (5 * density.density).toInt(),
    (50 * density.density).toInt(),
    Bitmap.Config.ARGB_8888
  )
  Canvas(bitmap).run {
    this.drawARGB((255 * 0.3).roundToInt(), 50, 50, 50)
  }

  return BitmapDrawable(Globals.context.resources, bitmap)
}