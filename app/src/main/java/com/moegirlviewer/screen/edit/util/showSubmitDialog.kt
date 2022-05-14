package com.moegirlviewer.screen.edit.util

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.theme.background2
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.printDebugLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalPagerApi
fun showSubmitDialogOfEdit(model: EditScreenModel) {
  var summary by mutableStateOf("")
  var isMinor by mutableStateOf(true)
  val quickInsertList = Gson().fromJson(Globals.context.getString(R.string.jsonArray_quickSummaryListOfEdit), Array<String>::class.java)

  model.quickInsertBarVisibleAllowed = false

  fun submit() {
    model.coroutineScope.launch {
     val isCloseDialog = model.submit(summary, isMinor)
      if (isCloseDialog) {
        Globals.commonAlertDialog.hide()
        delay(300)
        model.quickInsertBarVisibleAllowed = true
      }
    }
  }

  Globals.commonAlertDialog.show(CommonAlertDialogProps(
    title = Globals.context.getString(R.string.submitEdit),
    closeOnDismiss = false,
    closeOnAction = false,
    content = {
      val themeColors = MaterialTheme.colors

      Column {
        PlainTextField(
          modifier = Modifier
            .padding(vertical = 5.dp),
          value = summary,
          placeholder = stringResource(id = R.string.inputSummaryPlease),
          singleLine = true,
          underline = true,
          maxLength = 200,
          lengthIndicator = true,
          textStyle = TextStyle(
            fontSize = 16.sp
          ),
          keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onAny = { submit() }
          ),
          onValueChange = {
            summary = it
          }
        )

        StyledText(
          text = stringResource(id = R.string.quickInsert),
          fontSize = 16.sp
        )

        Row(
          modifier = Modifier
            .horizontalScroll(rememberScrollState())
        ) {
          for (item in quickInsertList) {
            StyledText(
              modifier = Modifier
                .padding(top = 10.dp, end = 5.dp)
                .clip(CircleShape)
                .clickable { summary += item }
                .background(themeColors.background2)
                .padding(vertical = 5.dp, horizontal = 10.dp),
              text = item,
            )
          }
        }

        Box(
          modifier = Modifier
            .offset(x = (-12).dp)
        ) {
          Row(
            modifier = Modifier
              .noRippleClickable { isMinor = !isMinor },
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = isMinor,
              onCheckedChange = { isMinor = it }
            )

            StyledText(
              text = stringResource(id = R.string.markAsMinorEdit),
            )
          }
        }
      }
    },
    secondaryButton = ButtonConfig.cancelButton(
      onClick = {
        Globals.commonAlertDialog.hide()
        model.coroutineScope.launch {
          delay(300)
          model.quickInsertBarVisibleAllowed = true
        }
      }
    ),
    primaryButtonText = Globals.context.getString(R.string.submit),
    onPrimaryButtonClick = { submit() }
  ))
}