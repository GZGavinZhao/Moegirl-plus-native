package com.moegirlviewer.screen.article.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.styled.StyledText

@Composable
fun ArticleLoadingMask() {
  val themeColors = MaterialTheme.colors
  val configuration = LocalConfiguration.current

  Center(
    modifier = Modifier
      .background(themeColors.background)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      AsyncImage(
        modifier = Modifier
          .width((configuration.screenWidthDp * 0.4).dp),
        model = rememberImageRequest(R.drawable.article_loading),
        contentDescription = null
      )

      StyledText(
        modifier = Modifier
          .padding(top = 10.dp),
        text = stringResource(id = R.string.articleLoading) + "...",
        color = themeColors.primaryVariant,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}