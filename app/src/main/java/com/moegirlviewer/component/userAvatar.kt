package com.moegirlviewer.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.util.gotoUserPage
import com.moegirlviewer.util.noRippleClickable

@OptIn(ExperimentalCoilApi::class)
@Composable
fun UserAvatar(
  modifier: Modifier = Modifier,
  userName: String,
  onClick: (() -> Unit)? = { gotoUserPage(userName) }
) {
  AsyncImage(
    modifier = Modifier
      .then(modifier)
      .clip(CircleShape)
      .noRippleClickable { onClick?.invoke() },
    model = rememberImageRequest(Constants.avatarUrl + userName) {
      error(R.drawable.akari)
    },
    placeholder = painterResource(R.drawable.akari),
    contentDescription = null
  )
}