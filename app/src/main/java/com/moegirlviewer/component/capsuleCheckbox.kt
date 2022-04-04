package com.moegirlviewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.component.styled.StyledText

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
        .clip(CircleShape)
        .clickable { onCheckedChange(!checked) }
        .background(if (themeColors.isLight) themeColors.secondary else themeColors.background)
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
            checkedColor = if (themeColors.isLight) themeColors.onSecondary else themeColors.surface,
            uncheckedColor = if (themeColors.isLight) themeColors.onSecondary else themeColors.surface,
            checkmarkColor = themeColors.secondary
          ),
          onCheckedChange = onCheckedChange
        )
      }

      StyledText(
        modifier = Modifier
          .padding(end = 5.dp),
        text = text,
        color = themeColors.onSecondary,
        fontSize = 14.sp
      )
    }
  }
}