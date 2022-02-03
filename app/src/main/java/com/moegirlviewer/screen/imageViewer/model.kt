package com.moegirlviewer.screen.imageViewer

import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalPagerApi::class)
class ImageViewerScreenModel @Inject constructor() : ViewModel()  {
  lateinit var routeArguments: ImageViewerRouteArguments
  val pagerState = PagerState()

  override fun onCleared() {
    super.onCleared()
    routeArguments.removeReferencesFromArgumentPool()
  }
}