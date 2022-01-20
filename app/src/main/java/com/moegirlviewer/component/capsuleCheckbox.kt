package com.moegirlviewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CapsuleCheckbox(
  text: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  val themeColors = MaterialTheme.colors

  RippleColorScope(color = Color.White) {
    Row(
      modifier = Modifier
        .height(34.dp)
        .clickable { onCheckedChange(!checked) }
        .clip(CircleShape)
        .background(themeColors.primary)
        .padding(horizontal = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(30.dp),
        contentAlignment = Alignment.Center
      ) {
        Checkbox(
          checked = checked,
          colors = CheckboxDefaults.colors(
            checkedColor = themeColors.onPrimary,
            uncheckedColor = themeColors.onPrimary,
            checkmarkColor = themeColors.secondary
          ),
          onCheckedChange = onCheckedChange
        )
      }

      Text(
        modifier = Modifier
          .padding(end = 5.dp),
        text = text,
        color = themeColors.onPrimary,
        fontSize = 14.sp
      )
    }
  }
}