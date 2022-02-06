package com.moegirlviewer.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.NospzGothicMoeFamily

@Composable
fun PlainTextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String? = null,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = TextStyle.Default,
  placeholderStyle: TextStyle = textStyle.copy(color = MaterialTheme.colors.text.tertiary),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  underline: Boolean = false,
  maxLength: Int? = null,
  lengthIndicator: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  cursorBrush: Brush = SolidColor(MaterialTheme.colors.secondary),
  decorationBox: (@Composable (@Composable () -> Unit) -> Unit) = { it() }
) {
  var focused by remember { mutableStateOf(false) }

  BasicTextField(
    value = value,
    modifier = Modifier
      .onFocusChanged {
        if (underline) focused = it.isFocused
      }
      .then(modifier),
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle.merge(LocalTextStyle.current),
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    visualTransformation = visualTransformation,
    interactionSource = interactionSource,
    cursorBrush = cursorBrush,
    onTextLayout = onTextLayout,
    onValueChange = {
      if (maxLength != null) {
        if (it.length <= maxLength) onValueChange(it)
      } else {
        onValueChange(it)
      }
    },
    decorationBox = @Composable { self ->
      decorationBox {
        TextFieldDecoration(
          placeholder = placeholder,
          placeholderStyle = placeholderStyle,
          content = self,
          textLength = value.length,
          focused = focused,
          underline = underline,
          lengthIndicator = lengthIndicator,
          maxLength = maxLength
        )
      }
    }
  )
}

@Composable
fun PlainTextField(
  modifier: Modifier = Modifier,
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  placeholder: String? = null,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = TextStyle.Default,
  placeholderStyle: TextStyle = textStyle.copy(color = MaterialTheme.colors.text.tertiary),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  underline: Boolean = false,
  maxLength: Int? = null,
  lengthIndicator: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  cursorBrush: Brush = SolidColor(MaterialTheme.colors.secondary),
  decorationBox: (@Composable (@Composable () -> Unit) -> Unit) = { it() }
) {
  var focused by remember { mutableStateOf(false) }

  BasicTextField(
    value = value,
    modifier = modifier
      .onFocusChanged {
        if (underline) focused = it.isFocused
      }
      .then(modifier),
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle.merge(LocalTextStyle.current),
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    visualTransformation = visualTransformation,
    interactionSource = interactionSource,
    cursorBrush = cursorBrush,
    onTextLayout = onTextLayout,
    onValueChange = {
      if (maxLength != null) {
        if (it.text.length <= maxLength) onValueChange(it)
      } else {
        onValueChange(it)
      }
    },
    decorationBox = { self ->
      decorationBox {
        TextFieldDecoration(
          placeholder = placeholder,
          placeholderStyle = placeholderStyle,
          content = self,
          textLength = value.text.length,
          focused = focused,
          underline = underline,
          lengthIndicator = lengthIndicator,
          maxLength = maxLength
        )
      }
    }
  )
}

@Composable
private fun TextFieldDecoration(
  placeholder: String?,
  placeholderStyle: TextStyle,
  textLength: Int,
  focused: Boolean,
  underline: Boolean,
  lengthIndicator: Boolean,
  maxLength: Int?,
  content: @Composable () -> Unit
) {
  val themeColors = MaterialTheme.colors
  val underlineHeight by animateDpAsState(if (focused) 2.dp else 1.dp)
  val underlineColor by animateColorAsState(if (focused) themeColors.secondary else themeColors.text.tertiary)

  Column() {
    Box(
      contentAlignment = Alignment.CenterStart
    ) {
      content()
      if (textLength == 0 && placeholder != null) {
        StyledText(
          text = placeholder,
          style = placeholderStyle,
          color = themeColors.text.tertiary,
        )
      }
    }

    if (underline) {
      Spacer(modifier = Modifier
        .padding(top = 10.dp)
        .fillMaxWidth()
        .height(underlineHeight)
        .background(underlineColor)
      )
    }

    if (maxLength != null && lengthIndicator) {
      Box(
        modifier = Modifier
          .fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd
      ) {
        StyledText(
          text = "${textLength}/${maxLength}",
          fontSize = 12.sp,
          color = underlineColor
        )
      }
    }
  }
}
