package com.moegirlviewer.screen.article.component.findBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTextButton
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.autoFocus

@Composable
fun ArticleScreenFindBar(
  visible: Boolean,
  onFindAll: (keyword: String) -> Unit,
  onFindNext: () -> Unit,
  onClose: () -> Unit
) {
  val themeColors = MaterialTheme.colors
  var inputValue by rememberSaveable { mutableStateOf("") }
  val yOffset = Globals.statusBarHeight + Constants.topAppBarHeight + 10

  LaunchedEffect(visible) {
    inputValue = ""
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.TopEnd,
  ) {
    AnimatedVisibility(
      modifier = Modifier
        .offset(x = (-10).dp, y = yOffset.dp),
      visible = visible,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Surface(
        modifier = Modifier
          .width(270.dp)
          .height(40.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(10)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onClose
          ) {
            Icon(
              modifier = Modifier
                .size(14.dp),
              imageVector = Icons.Filled.Close,
              contentDescription = null,
              tint = themeColors.text.tertiary
            )
          }

          PlainTextField(
            modifier = Modifier
              .weight(1f)
              .autoFocus(300),
            value = inputValue,
            singleLine = true,
            onValueChange = {
              inputValue = it
              onFindAll(it)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onAny = { onFindNext() }
            )
          )

          StyledTextButton(
            modifier = Modifier
              .padding(end = 2.dp),
            onClick = { onFindNext() }
          ) {
            StyledText(
              text = stringResource(id = R.string.findNext),
              fontSize = 13.sp,
              color = Color.Unspecified
            )
          }
        }
      }
    }
  }
}