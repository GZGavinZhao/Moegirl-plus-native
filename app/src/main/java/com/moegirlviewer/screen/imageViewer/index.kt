package com.moegirlviewer.screen.imageViewer

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.swipeable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.compose.rememberImagePainter
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.moegirlviewer.R
import com.moegirlviewer.component.articleView.MoegirlImage
import com.moegirlviewer.util.noRippleClickable
import java.io.File

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
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
        this.setCurrentItem(initialIndex, false)

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

@ExperimentalComposeUiApi
@Composable
fun Test() {
  var offsetX by remember { mutableStateOf(0.dp) }
  var offsetY by remember { mutableStateOf(0.dp) }
  var scale by remember { mutableStateOf(1f) }
  var rotationState by remember { mutableStateOf(1f) }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomStart
  ) {
    Box(
      modifier = Modifier
        .clip(RectangleShape) // Clip the box content
        .fillMaxSize() // Give the size you want...
        .background(Color.Gray)
        .pointerInput(Unit) {
          detectTransformGestures { centroid, pan, zoom, rotation ->
            offsetX += pan.x.toDp()
            offsetY += pan.y.toDp()
            scale *= zoom
            rotationState += rotation
          }
        }
    ) {
      Image(
        modifier = Modifier
          .offset(offsetX, offsetY)
          .align(Alignment.Center)
          .fillMaxSize()
          .graphicsLayer(
            // adding some zoom limits (min 50%, max 200%)
            scaleX = scale.coerceIn(0.5f, 3f),
            scaleY = scale.coerceIn(0.5f, 3f),
            rotationZ = rotationState
          ),
        painter = rememberImagePainter("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF"),
        contentDescription = null,
        contentScale = ContentScale.FillWidth
      )
    }
  }
}