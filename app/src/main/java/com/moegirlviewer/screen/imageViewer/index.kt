package com.moegirlviewer.screen.imageViewer

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.moegirlviewer.R
import com.moegirlviewer.component.articleView.MoegirlImage
import java.io.File

@Composable
fun ImageViewerScreen(
  arguments: ImageViewerRouteArguments
) {
  val model: ImageViewerScreenModel = hiltViewModel()
  val configuration = LocalConfiguration.current

  LaunchedEffect(true) {
    model.routeArguments = arguments
    model.currentImgIndex = arguments.initialIndex
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomStart
  ) {
    ComposePagedBigImageViews(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black),
      images = arguments.images,
      initialIndex = arguments.initialIndex,
      onPageChange = { model.currentImgIndex = it }
    )

    if (arguments.images.size > 1) {
      Column(
        modifier = Modifier
          .offset(20.dp, (-20).dp)
          .width((configuration.screenWidthDp * 0.6).dp),
      ) {
        Text(
          text = stringResource(id = R.string.gallery) + "：${model.currentImgIndex + 1} / ${arguments.images.size}",
          color = Color(0xffcccccc)
        )
        Text(
         text = arguments.images[model.currentImgIndex].title,
         color = Color(0xffcccccc)
        )
      }
    }
  }
}

@Composable
private fun ComposePagedBigImageViews(
  modifier: Modifier = Modifier,
  images: List<MoegirlImage>,
  initialIndex: Int,
  onPageChange: (position: Int) -> Unit
) {
  AndroidView(
    modifier = modifier,
    factory = {
      ViewPager2(it).apply {
        this.layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        this.adapter = PagerAdapter(images)
        this.currentItem = initialIndex

        this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
          override fun onPageSelected(position: Int) {
            onPageChange(position)
          }
        })
      }
    }
  )
}

private fun createBigImageView(
  context: Context
): BigImageView {
  return BigImageView(context).apply {
    this.layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )

    this.setOptimizeDisplay(true)
    this.setImageViewFactory(FrescoImageViewFactory())
    this.setProgressIndicator(ProgressPieIndicator())

    this.configSSIV {
      this.maxScale = 10f
    }
  }
}

private class BigImageViewHolder(
  val view: BigImageView
) : RecyclerView.ViewHolder(view)

private class PagerAdapter(
  val images: List<MoegirlImage>
) : RecyclerView.Adapter<BigImageViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigImageViewHolder {
    return BigImageViewHolder(createBigImageView(parent.context))
  }

  override fun onBindViewHolder(holder: BigImageViewHolder, position: Int) {
    holder.view.showImage(images[position].fileUrl.toUri())
  }

  override fun getItemCount() = images.size
}

private fun BigImageView.configSSIV(
  configure: SubsamplingScaleImageView.() -> Unit,
) {
  this.setImageLoaderCallback(object : ImageLoader.Callback {
    override fun onCacheHit(imageType: Int, image: File?) {}
    override fun onCacheMiss(imageType: Int, image: File?) {}
    override fun onStart() {}
    override fun onProgress(progress: Int) {}
    override fun onFinish() {}

    override fun onSuccess(image: File?) {
      configure.invoke(this@configSSIV.ssiv)
    }

    override fun onFail(error: Exception?) {}
  })
}