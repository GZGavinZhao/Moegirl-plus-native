package com.moegirlviewer.screen.login.component

import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.moegirlviewer.util.isMoegirl

@Composable
fun LoginScreenTextField(
  modifier: Modifier = Modifier,
  value: String,
  label: String,
  password: Boolean = false,
  onValueChange: (value: String) -> Unit,
  onAction: (KeyboardActionScope.() -> Unit)? = null
) {
  var showingPassword by remember { mutableStateOf(false) }
  val unfocusedColor = Color.White
  val focusedColor = if (isMoegirl()) Color(0xFFC8E6C9) else Color(0xffFFE686)

  OutlinedTextField(
    modifier = modifier,
    value = value,
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = if (password)
        KeyboardType.Password else
        KeyboardType.Text,
      imeAction = if (password)
        ImeAction.Done else
        ImeAction.Next,
    ),
    keyboardActions = if (onAction != null) KeyboardActions(
      onAny = onAction,
    ) else KeyboardActions(),
    label = {
      Text(text = label)
    },
    colors = TextFieldDefaults.outlinedTextFieldColors(
      unfocusedBorderColor = unfocusedColor,
      focusedBorderColor = focusedColor,
      textColor = unfocusedColor,
      cursorColor = focusedColor,
      unfocusedLabelColor = unfocusedColor,
      focusedLabelColor = focusedColor,
    ),
    trailingIcon = {
      if (password) {
        IconButton(
          onClick = { showingPassword = !showingPassword }
        ) {
          Icon(
            imageVector = if (showingPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            tint = if (showingPassword) focusedColor else unfocusedColor,
            contentDescription = null
          )
        }
      }
    },
    visualTransformation = if (password && !showingPassword)
      PasswordVisualTransformation() else
      VisualTransformation.None,
    onValueChange = onValueChange
  )
}