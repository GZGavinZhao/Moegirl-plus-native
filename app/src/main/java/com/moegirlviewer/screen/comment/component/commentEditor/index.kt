package com.moegirlviewer.screen.comment.component.commentEditor

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.R
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.util.*
import kotlinx.coroutines.FlowPreview

@ExperimentalPagerApi
@FlowPreview
@Composable
fun CommentEditor(
  visible: Boolean,
  value: String,
  title: String,
  placeholder: String,
  onTextChange: ((text: String) -> Unit)? = null,
  onSubmit: () -> Unit,
  onDismiss: () -> Unit,
) {
  val model: EditScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val transition = updateTransition(visible)
  val height = 150

  fun <T> animation() = tween<T>(
    durationMillis = 300
  )

  val topOffset by transition.animateDp({ animation() }) {
    if (it) 0.dp else height.dp
  }

  val maskAlpha by transition.animateFloat({ animation() }) {
    if (it) 0.5f else 0f
  }

  BackHandler(visible) {
    onDismiss()
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    if (maskAlpha != 0f) {
      Spacer(modifier = Modifier
        .fillMaxSize()
        .background(Color(0f, 0f, 0f, maskAlpha))
        .noRippleClickable { onDismiss() }
      )
    }

    if (topOffset.value.toInt() != height) {
      Box(
        modifier = Modifier
          .imeBottomPadding()
          .fillMaxWidth()
          .height(height.dp)
          .offset(y = topOffset)
          .clip(
            RoundedCornerShape(
              topStart = 10.dp,
              topEnd = 10.dp
            )
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(vertical = 10.dp, horizontal = 15.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = title,
            )

            Box(
              modifier = Modifier
                .height(25.dp)
                .noRippleClickable { onSubmit() },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = stringResource(id = R.string.publish),
                color = themeColors.secondary
              )
            }
          }

          Box(
            modifier = Modifier
              .padding(top = 3.dp)
              .weight(1f)
          ) {
            PlainTextField(
              modifier = Modifier
                .fillMaxSize()
                .autoFocus(),
              value = value,
              placeholder = placeholder,
              textStyle = TextStyle(
                fontSize = 16.sp
              ),
              onValueChange = { onTextChange?.invoke(it) },
            )
          }
        }
      }
    }
  }
}

private data class State(
  val visible: Boolean = false,
  val title: String = "",
  val placeholder: String = "",
  val onSubmit: ((text: String) -> Unit) = {},
  val onDismiss: (() -> Unit) = {}
)

@ExperimentalPagerApi
@Composable
fun useCommentEditor(): CommentEditorSet {
  var state by remember { mutableStateOf(State()) }
  var inputVal by remember { mutableStateOf("") }

  val controller = remember { object : CommentEditorController() {
      override fun show(
        targetName: String,
        isReply: Boolean,
        onSubmit: (inputVal: String) -> Unit,
      ) {
        val title = Globals.context.getString(
          if (isReply) R.string.reply else R.string.comment
        )
        state = State(
          visible = true,
          title = title,
          placeholder = "$title：$targetName",
          onSubmit = {
            onSubmit(it)
          },
        )
      }

      override fun hide() {
        state = state.copy(visible = false)
      }

      override fun clearInputVal() {
        inputVal = ""
      }
    }
  }

  return CommentEditorSet(controller) {
    CommentEditor(
      visible = state.visible,
      title = state.title,
      value = inputVal,
      placeholder = state.placeholder,
      onTextChange = { inputVal = it },
      onSubmit = { state.onSubmit(inputVal) },
      onDismiss = {
        state = state.copy(visible = false)
        state.onDismiss()
      }
    )
  }
}

abstract class CommentEditorController {
  abstract fun show(
    targetName: String,
    isReply: Boolean = false,
    onSubmit: (inputVal: String) -> Unit
  )

  abstract fun hide()
  abstract fun clearInputVal()
}

data class CommentEditorSet(
  val controller: CommentEditorController,
  val Holder: @Composable () -> Unit
)

class CommentEditorExceptionByUserClosed : Exception()