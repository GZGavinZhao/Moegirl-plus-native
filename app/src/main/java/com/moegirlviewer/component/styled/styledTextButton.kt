package com.moegirlviewer.component.styled

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.moegirlviewer.component.RippleColorScope

@Composable
fun StyledTextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  elevation: ButtonElevation? = null,
  shape: Shape = MaterialTheme.shapes.small,
  border: BorderStroke? = null,
  colors: ButtonColors = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colors.secondary
  ),
  contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
  content: @Composable RowScope.() -> Unit
) {
  val themeColors = MaterialTheme.colors

  RippleColorScope(color = themeColors.secondary) {
    TextButton(
      onClick = onClick,
      modifier = modifier,
      enabled = enabled,
      interactionSource = interactionSource,
      elevation = elevation,
      shape = shape,
      border = border,
      colors = colors,
      contentPadding = contentPadding,
      content = content
    )
  }
}