package com.moegirlviewer.component.commonDialog

import android.app.Dialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moegirlviewer.R
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.visibility

class CommonAlertDialogProps(
  val title: String? = null,
  val primaryButtonText: String? = null,
  val secondaryButton: ButtonConfig? = null,
  val leftButton: ButtonConfig? = null,
  val closeOnDismiss: Boolean = true,
  val closeOnAction: Boolean = true,
  val onDismiss: (() -> Unit)? = null,
  val onPrimaryButtonClick: (() -> Unit)? = null,
  val content: (@Composable () -> Unit)? = null,
)

class CommonAlertDialogRef(
  val show: (props: CommonAlertDialogProps) -> Unit,
  val showText: (text: String) -> Unit,
  val hide: () -> Unit
)

@ExperimentalComposeUiApi
@Composable
fun CommonAlertDialog(
  ref: Ref<CommonAlertDialogRef>
) {
  var visible by remember { mutableStateOf(false) }
  var currentProps by remember { mutableStateOf<CommonAlertDialogProps?>(null) }

  fun show(props: CommonAlertDialogProps) {
    visible = true
    currentProps = props
  }

  fun hide() {
    visible = false
  }

  ref.value = remember {
    CommonAlertDialogRef(
      show = { show(it) },
      showText = {
        show(CommonAlertDialogProps(
          content = { Text(it) }
        ))
      },
      hide = { hide() }
    )
  }

  if (!visible || currentProps == null) {
    return
  }

  BackHandler(
    onBack = { visible = false }
  )

  val props = currentProps!!
  CommonAlertDialogUI(
    title = props.title,
    secondaryButton = props.secondaryButton,
    leftButton = props.leftButton,
    onDismiss = {
      if (props.closeOnDismiss) visible = false
      props.onDismiss?.invoke()
    },
    primaryButtonText = props.primaryButtonText,
    onRequestClose = { visible = false },
    onPrimaryButtonClick = {
      if (currentProps!!.onPrimaryButtonClick != null) {
        currentProps!!.onPrimaryButtonClick?.invoke()
      }
      if (props.closeOnAction) visible = false
    },
  ) {
    props.content?.invoke()
  }
}

@ExperimentalComposeUiApi
@Composable
fun CommonAlertDialogUI(
  title: String? = null,
  primaryButtonText: String? = null,
  secondaryButton: ButtonConfig? = null,
  leftButton: ButtonConfig? = null,
  closeOnAction: Boolean = true,
  onDismiss: () -> Unit,
  onPrimaryButtonClick: () -> Unit,
  onRequestClose: (() -> Unit)? = null,
  content: (@Composable () -> Unit)? = null,
) {
  val themeColors = MaterialTheme.colors
  val configuration = LocalConfiguration.current
  val textStyle = LocalTextStyle.current
  val defaultTextStyle = remember {
    textStyle.copy(
      fontSize = 16.sp
    )
  }

  // AlertDialog在嵌套滚动视图时会出bug，必须用Dialog
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(5.dp)
    ) {
      Column(
        modifier = Modifier
          .width((configuration.screenWidthDp * 0.85).dp)
          .background(themeColors.surface)
      ) {
        Column(
          modifier = Modifier
            .padding(horizontal = 20.dp)
        ) {
          Text(
            modifier = Modifier
              .padding(vertical = 18.dp),
            text = title ?: stringResource(id = R.string.alert),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )

          CompositionLocalProvider(
            LocalTextStyle provides defaultTextStyle
          ) {
            content?.invoke()
          }
        }

        Row(
          modifier = Modifier
            .padding(horizontal = 10.dp)
            .padding(top = 15.dp, bottom = 5.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          TextButton(
            modifier = Modifier
              .visibility(leftButton != null),
            enabled = leftButton != null,
            onClick = {
              leftButton?.onClick?.invoke()
              if (closeOnAction) onRequestClose?.invoke()
            },
          ) {
            Text(
              text = leftButton?.text ?: "",
              fontWeight = FontWeight.Bold,
            )
          }

          Row() {
            if (secondaryButton != null) {
              TextButton(
                modifier = Modifier
                  .padding(end = 5.dp),
                onClick = {
                  secondaryButton.onClick?.invoke()
                  if (closeOnAction) onRequestClose?.invoke()
                }
              ) {
                Text(
                  text = secondaryButton.text,
                  fontWeight = FontWeight.Bold,
                  color = themeColors.text.secondary
                )
              }
            }

            TextButton(
              onClick = onPrimaryButtonClick
            ) {
              Text(
                text = primaryButtonText ?: stringResource(R.string.check),
                fontWeight = FontWeight.Bold,
                color = themeColors.secondary
              )
            }
          }
        }
      }
    }
  }
}

class ButtonConfig(
  val text: String,
  val onClick: (() -> Unit)? = null
) {
  companion object {
    fun cancelButton(
      onClick: (() -> Unit)? = null
    ) = ButtonConfig(
      text = Globals.context.getString(R.string.cancel),
      onClick = onClick
    )
  }
}