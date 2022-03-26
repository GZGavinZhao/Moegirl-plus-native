package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.moegirlviewer.util.styledPlaceholder

@Composable
fun CardPlaceholder() {
  Column(
    modifier = Modifier
      .padding(15.dp)
  ) {
    Row() {
      Spacer(modifier = Modifier
        .size(30.dp)
        .clip(CircleShape)
        .styledPlaceholder())

      Spacer(modifier = Modifier
        .padding(start = 10.dp)
        .height(30.dp)
        .width(120.dp)
        .styledPlaceholder())
    }

    Spacer(modifier = Modifier
      .padding(top = 15.dp)
      .fillMaxWidth()
      .height(250.dp)
      .clip(RoundedCornerShape(10.dp))
      .styledPlaceholder())
  }
}