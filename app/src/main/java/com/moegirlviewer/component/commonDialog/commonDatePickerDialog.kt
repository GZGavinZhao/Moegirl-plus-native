package com.moegirlviewer.component.commonDialog

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.moegirlviewer.R
import com.moegirlviewer.util.toEpochMilli
import kotlinx.coroutines.CompletableDeferred
import java.time.LocalDate

class CommonDatePickerDialogState() {
  internal var visible by mutableStateOf(false)
  internal var onCheck: (() -> Unit)? = null
  internal var onHide: (() -> Unit)? = null
  internal var deferredRefReady: CompletableDeferred<DatePicker>? = null
//  internal val datePickerRef: Ref<DatePicker> = Ref()

  suspend fun show(
    initialValue: LocalDate = LocalDate.now(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onValueChange: ((LocalDate) -> Unit)? = null,
  ): LocalDate? {
    val completableDeferred = CompletableDeferred<LocalDate?>()
    visible = true
    var currentValue = initialValue
    onCheck = { completableDeferred.complete(currentValue) }
    onHide = { completableDeferred.complete(null) }
    // 因为DatePicker在Dialog里，DatePicker实例必须在dialog显示后才能初始化，这里用一个deferred来等待实例创建完成
    deferredRefReady = CompletableDeferred()

    val datePicker = deferredRefReady!!.await()

    if (minDate != null) datePicker.minDate = minDate.toEpochMilli()
    if (maxDate != null) datePicker.maxDate = maxDate.toEpochMilli()
    datePicker.setOnDateChangedListener { _, year, month, date ->
      var newValue = LocalDate.of(year, month + 1, date)
      if (newValue.isBefore(minDate)) newValue = minDate
      if (newValue.isAfter(maxDate)) newValue = maxDate
      onValueChange?.invoke(newValue)
      currentValue = newValue
    }
    datePicker.updateDate(initialValue.year, initialValue.monthValue - 1, initialValue.dayOfMonth)

    return completableDeferred.await()
  }
}



@Composable
fun CommonDatePickerDialog(
  state: CommonDatePickerDialogState = CommonDatePickerDialogState()
) {
  val themeColors = MaterialTheme.colors

  if (state.visible) {
    Dialog(
      onDismissRequest = {
        state.visible = false
        state.onHide?.invoke()
      }
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(5.dp))
      ) {
        AndroidView(
          modifier = Modifier
            .background(themeColors.surface),
          factory = {
            DatePicker(it).also {
              state.deferredRefReady!!.complete(it)
            }
          }
        )

        Box(
          modifier = Modifier
            .matchParentSize(),
          contentAlignment = Alignment.TopEnd
        ) {
          TextButton(
            onClick = {
              state.visible = false
              state.onCheck?.invoke()
            }
          ) {
            Text(
              text = stringResource(id = R.string.check),
              color = themeColors.onPrimary
            )
          }
        }
      }
    }
  }
}