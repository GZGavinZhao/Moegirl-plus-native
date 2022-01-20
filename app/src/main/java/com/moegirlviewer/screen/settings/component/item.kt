package com.moegirlviewer.screen.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.ui.theme.background2
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.sideBorder

@Composable
fun SettingsScreenItem(
  title: String,
  titleStyle: TextStyle = TextStyle(),
  subtext: String? = null,
  onClick: (() -> Unit)? = null,
  rightContent: (@Composable () -> Unit)? = null,
) {
  val themeColors = MaterialTheme.colors

  Surface(
    modifier = Modifier
      .sideBorder(BorderSide.BOTTOM, 1.dp, themeColors.background2)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick?.invoke() }
        .padding(vertical = 10.dp, horizontal = 15.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier
          .weight(1f),
      ) {
        Text(
          text = title,
          color = themeColors.text.primary,
          style = titleStyle,
        )
        if (subtext != null) {
          Text(
            modifier = Modifier
              .padding(top = 3.dp),
            text = subtext,
            fontSize = 12.sp,
            color = themeColors.text.secondary
          )
        }
      }

      rightContent?.invoke()
    }
  }
}