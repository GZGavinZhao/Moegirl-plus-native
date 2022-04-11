package com.moegirlviewer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.isMoegirl

@Composable
fun EmptyContent(
  height: Dp = defaultEmptyContentHeight().dp,
  emptyContentImageSizeType: EmptyContentImageSizeType = EmptyContentImageSizeType.NORMAL,
  message: String = stringResource(id = R.string.emptyContnet)
) {
  val configuration = LocalConfiguration.current
  val themeColors = MaterialTheme.colors
  val usingImageSizes = isMoegirl(moegirlEmptyContentImageSizes, hmoeEmptyContentImageSizes)
  val usingSize = when(emptyContentImageSizeType) {
    EmptyContentImageSizeType.NORMAL -> usingImageSizes.normalImageSize
    EmptyContentImageSizeType.SMALL -> usingImageSizes.smallImageSize
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(height),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Image(
      modifier = Modifier
        .width(usingSize.width.dp)
        .height(usingSize.height.dp),
      painter = painterResource(R.drawable.empty),
      contentDescription = null,
    )

    StyledText(
      modifier = Modifier
        .padding(top = 20.dp),
      text = message,
      fontSize = 18.sp,
      color = themeColors.text.secondary
    )
  }
}

@Composable
private fun defaultEmptyContentHeight() = LocalConfiguration.current.screenHeightDp - 56

private class EmptyContentImageSizes(
  val originalWidth: Float,
  val originalHeight: Float,
  private val normalSizeFraction: Float,
  private val smallSizeFraction: Float
) {
  val normalImageSize = Size(
    originalWidth * normalSizeFraction,
    originalHeight * normalSizeFraction
  )
  val smallImageSize = Size(
    originalWidth * smallSizeFraction,
    originalHeight * smallSizeFraction,
  )
}

private val moegirlEmptyContentImageSizes = EmptyContentImageSizes(
  originalWidth = 500f,
  originalHeight = 500f,
  normalSizeFraction = 0.5f,
  smallSizeFraction = 0.3f
)

private val hmoeEmptyContentImageSizes = EmptyContentImageSizes(
  originalWidth = 500f,
  originalHeight = 300f,
  normalSizeFraction = 0.6f,
  smallSizeFraction = 0.5f
)

enum class EmptyContentImageSizeType {
  NORMAL,
  SMALL,
}