package com.moegirlviewer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.util.gotoUserPage
import com.moegirlviewer.util.noRippleClickable

@OptIn(ExperimentalCoilApi::class)
@Composable
fun UserAvatar(
  modifier: Modifier = Modifier,
  userName: String,
  onClick: (() -> Unit)? = { gotoUserPage(userName) }
) {
  val painter = rememberImagePainter(Constants.avatarUrl + userName) {
    crossfade(true)
    placeholder(R.drawable.akari)
    error(R.drawable.akari)
    fallback(R.drawable.akari)
  }

  Image(
    modifier = Modifier
      .then(modifier)
      .clip(CircleShape)
      .noRippleClickable { onClick?.invoke() },
    painter = painter,
    contentDescription = null
  )
}