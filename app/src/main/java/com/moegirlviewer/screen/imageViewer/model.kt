package com.moegirlviewer.screen.imageViewer

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.moegirlviewer.R
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalPagerApi::class)
class ImageViewerScreenModel @Inject constructor() : ViewModel()  {
  lateinit var routeArguments: ImageViewerRouteArguments
  val pagerState = PagerState()

  private suspend fun getCurrentImage(): Drawable? {
    val completableDeferred = CompletableDeferred<Drawable?>()
    val imageUrl = routeArguments.images[pagerState.currentPage].fileUrl
    val imageRequest = ImageRequest.Builder(Globals.context)
      .data(imageUrl)
      .target(
        onSuccess = {
          completableDeferred.complete(it)
        },
        onError = {
          completableDeferred.complete(null)
        }
      )
      .build()

    Globals.imageLoader.enqueue(imageRequest)
    return completableDeferred.await()
  }

  suspend fun downloadCurrentImage() {
    val imageUrl = routeArguments.images[pagerState.currentPage].fileUrl
    val drawable = getCurrentImage()
    if (drawable != null) {
      saveImage(drawable.toBitmap(), computeMd5(imageUrl))
      toast(Globals.context.getString(R.string.imageSavedToAlbum))
    } else {
      toast(Globals.context.getString(R.string.downloadFailed))
    }
  }

  suspend fun shareCurrentImage() {
    val imageUrl = routeArguments.images[pagerState.currentPage].fileUrl
    val drawable = getCurrentImage()
    if (drawable != null) {
      val shareImagesDir = File(Globals.context.externalCacheDir, "shareImages")
      shareImagesDir.mkdir()
      val file = File(shareImagesDir, computeMd5(imageUrl))
      file.createNewFile()
      val byteArr = run {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
      }

      file.writeBytes(byteArr)
      val uriAuthority = Globals.context.packageName + ".fileprovider"
      shareImage(FileProvider.getUriForFile(Globals.context, uriAuthority, file))
    } else {
      toast(Globals.context.getString(R.string.shareFailed))
    }
  }

  override fun onCleared() {
    super.onCleared()
    routeArguments.removeReferencesFromArgumentPool()
  }
}