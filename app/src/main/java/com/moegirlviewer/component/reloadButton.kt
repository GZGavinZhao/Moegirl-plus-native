package com.moegirlviewer.component

import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText

@Composable
fun ReloadButton(
  modifier: Modifier = Modifier,
  text: String = stringResource(id = R.string.reload),
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors

  TextButton(
    modifier = modifier,
    onClick = onClick
  ) {
    StyledText(
      text = text,
      fontSize = 15.sp,
      color = themeColors.secondary
    )
  }
}