package com.moegirlviewer.screen.newPages.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text

@Composable
fun NewPageItem(
  text: String,
  subtext: String? = null,
  imageUrl: String? = null,
  onClick: (() -> Unit)? = null
) {
  val themeColors = MaterialTheme.colors
  val density = LocalDensity.current.density

  RippleColorScope(color = themeColors.primaryVariant) {
    Row(
      modifier = Modifier
        .padding(bottom = 1.dp)
        .height(80.dp)
        .fillMaxWidth()
        .clickable { onClick?.invoke() }
        .background(themeColors.surface)
        .padding(end = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(60.dp)
          .fillMaxHeight()
      ) {
        if (imageUrl != null) {
          AsyncImage(
            modifier = Modifier
              .fillMaxSize(),
            model = rememberImageRequest(data = imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(themeColors.background2),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              modifier = Modifier
                .fillMaxSize(0.8f),
              imageVector = Icons.Filled.TextSnippet,
              contentDescription = null,
              tint = themeColors.text.tertiary
            )
          }
        }
      }

      Column(
        modifier = Modifier
          .padding(start = 10.dp),
        verticalArrangement = Arrangement.Center
      ) {
        StyledText(
          text = text,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (subtext != null && subtext != "") {
          StyledText(
            modifier = Modifier
              .padding(top = 3.dp),
            text = subtext,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = themeColors.text.secondary.copy(0.8f),
            fontSize = 13.sp
          )
        }
      }
    }
  }
}