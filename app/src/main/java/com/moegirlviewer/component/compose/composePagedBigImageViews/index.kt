package com.moegirlviewer.component.compose.composePagedBigImageViews
//
//import android.content.Context
//import android.view.ViewGroup
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.net.toUri
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
//import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
//import com.github.piasy.biv.loader.ImageLoader
//import com.github.piasy.biv.view.BigImageView
//import com.github.piasy.biv.view.FrescoImageViewFactory
//import com.moegirlviewer.component.articleView.MoegirlImage
//import java.io.File
//
//@Composable
//fun ComposePagedBigImageViews(
//  modifier: Modifier = Modifier,
//  images: List<MoegirlImage>,
//  initialIndex: Int,
//  onPageChange: (position: Int) -> Unit
//) {
//  AndroidView(
//    modifier = modifier,
//    factory = {
//      ViewPager2(it).apply {
//        this.layoutParams = ViewGroup.LayoutParams(
//          ViewGroup.LayoutParams.MATCH_PARENT,
//          ViewGroup.LayoutParams.MATCH_PARENT
//        )
//        this.orientation = ViewPager2.ORIENTATION_HORIZONTAL
//        this.adapter = PagerAdapter(images)
//        this.setCurrentItem(initialIndex, false)
//
//        this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//          override fun onPageSelected(position: Int) {
//            onPageChange(position)
//          }
//        })
//      }
//    }
//  )
//}
//
//private fun createBigImageView(
//  context: Context
//): BigImageView {
//  return BigImageView(context).apply {
//    this.layoutParams = ViewGroup.LayoutParams(
//      ViewGroup.LayoutParams.MATCH_PARENT,
//      ViewGroup.LayoutParams.MATCH_PARENT
//    )
//
//    this.setOptimizeDisplay(true)
//    this.setImageViewFactory(FrescoImageViewFactory())
//    this.setProgressIndicator(ProgressPieIndicator())
//
//    this.configSSIV {
//      this.maxScale = 10f
//    }
//  }
//}
//
//private class BigImageViewHolder(
//  val view: BigImageView
//) : RecyclerView.ViewHolder(view)
//
//private class PagerAdapter(
//  val images: List<MoegirlImage>
//) : RecyclerView.Adapter<BigImageViewHolder>() {
//  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigImageViewHolder {
//    return BigImageViewHolder(createBigImageView(parent.context))
//  }
//
//  override fun onBindViewHolder(holder: BigImageViewHolder, position: Int) {
//    holder.view.showImage(images[position].fileUrl.toUri())
//  }
//
//  override fun getItemCount() = images.size
//}
//
//private fun BigImageView.configSSIV(
//  configure: SubsamplingScaleImageView.() -> Unit,
//) {
//  this.setImageLoaderCallback(object : ImageLoader.Callback {
//    override fun onCacheHit(imageType: Int, image: File?) {}
//    override fun onCacheMiss(imageType: Int, image: File?) {}
//    override fun onStart() {}
//    override fun onProgress(progress: Int) {}
//    override fun onFinish() {}
//
//    override fun onSuccess(image: File?) {
//      configure.invoke(this@configSSIV.ssiv)
//    }
//
//    override fun onFail(error: Exception?) {}
//  })
//}