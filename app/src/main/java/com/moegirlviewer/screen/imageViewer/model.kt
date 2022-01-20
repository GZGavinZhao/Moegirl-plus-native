package com.moegirlviewer.screen.imageViewer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.node.Ref
import androidx.lifecycle.ViewModel
import com.github.piasy.biv.view.BigImageView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewerScreenModel @Inject constructor() : ViewModel()  {
  lateinit var routeArguments: ImageViewerRouteArguments
  var currentImgIndex by mutableStateOf(0)

  override fun onCleared() {
    super.onCleared()
    routeArguments.removeReferencesFromArgumentPool()
  }
}