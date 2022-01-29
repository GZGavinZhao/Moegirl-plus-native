package com.moegirlviewer.component.commonDialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText

class CommonLoadingDialogProps(
  val title: String? = null,
  val closeOnDismiss: Boolean = true,
  val onDismiss: (() -> Unit)? = null
)

class CommonLoadingDialogRef(
  private val _show: (props: CommonLoadingDialogProps?) -> Unit,
  val hide: () -> Unit
) {
  fun show(props: CommonLoadingDialogProps? = null) {
    _show(props)
  }

  fun showText(text: String) {
    _show(CommonLoadingDialogProps(text))
  }
}

@Composable
fun CommonLoadingDialog(
  ref: Ref<CommonLoadingDialogRef>
) {
  var visible by remember { mutableStateOf(false) }
  var currentProps by remember { mutableStateOf<CommonLoadingDialogProps?>(null) }

  fun show(props: CommonLoadingDialogProps? = null) {
    visible = true
    currentProps = props
  }

  fun hide() {
    visible = false
  }

  ref.value = remember {
    CommonLoadingDialogRef(
      _show = { show(it) },
      hide = { hide() }
    )
  }

  if (!visible) { return }

  BackHandler(
    onBack = { visible = false }
  )

  val props = currentProps ?: CommonLoadingDialogProps()
  CommonLoadingDialogUI(
    title = props.title,
    onDismiss = {
      if (props.closeOnDismiss) {
        visible = false
        props.onDismiss?.invoke()
      }
    }
  )
}

@Composable
fun CommonLoadingDialogUI(
  title: String?,
  onDismiss: () -> Unit,
) {
  val configuration = LocalConfiguration.current
  val dialogWidth = configuration.screenWidthDp * 0.7

  AlertDialog(
    modifier = Modifier
      .width(dialogWidth.dp),
    title = {
      Row(
        modifier = Modifier
          .padding(bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        StyledCircularProgressIndicator()

        StyledText(
          modifier = Modifier
            .padding(start = 20.dp),
          text = (title ?: stringResource(R.string.loading)) + "...",
          fontSize = 17.sp
        )
      }
    },
    buttons = {},
    onDismissRequest = onDismiss
  )
}