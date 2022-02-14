package com.moegirlviewer.screen.recentChanges.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.moegirlviewer.R
import com.moegirlviewer.component.CapsuleCheckbox
import com.moegirlviewer.component.ComposeSlider
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.RecentChangesSettings
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first

suspend fun showRecentChangesOptionsDialog(): RecentChangesSettings? {
  val completableDeferred = CompletableDeferred<RecentChangesSettings?>()
  var settings by mutableStateOf(SettingsStore.recentChanges.getValue { this }.first())

  Globals.commonAlertDialog.show(CommonAlertDialogProps(
    title = Globals.context.getString(R.string.listOptions),
    content = {
      val themeColors = MaterialTheme.colors
      val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)

      Column() {
        StyledText(
          fontSize = 16.sp,
          color = themeColors.text.secondary,
          text = buildAnnotatedString {
            append(stringResource(id = R.string.timeRange) + "：")
            withStyle(SpanStyle(
              color = themeColors.secondary,
              fontWeight = FontWeight.Bold
            )) {
              append(settings.daysAgo.toString())
            }
            append(stringResource(id = R.string.withinDay))
          }
        )

        ComposeSlider(
          value = settings.daysAgo.toFloat(),
          valueRange = 1f..7f,
          stepSize = 1f,
          onValueChange = {
            settings = settings.copy(daysAgo = it.toInt())
          }
        )

        StyledText(
          fontSize = 16.sp,
          color = themeColors.text.secondary,
          text = buildAnnotatedString {
            append(stringResource(id = R.string.maxShownNumber) + "：")
            withStyle(SpanStyle(
              color = themeColors.secondary,
              fontWeight = FontWeight.Bold
            )) {
              append(settings.totalLimit.toString())
            }
            append(stringResource(id = R.string.number))
          }
        )

        ComposeSlider(
          value = settings.totalLimit.toFloat(),
          valueRange = 50f..500f,
          stepSize = 50f,
          onValueChange = {
            settings = settings.copy(totalLimit = it.toInt())
          }
        )

        StyledText(
          text = stringResource(id = R.string.changeType),
          color = themeColors.text.secondary
        )
        FlowRow(
          modifier = Modifier
            .padding(top = 10.dp),
          mainAxisSpacing = 5.dp,
          crossAxisSpacing = 5.dp
        ) {
          if (isLoggedIn) {
            CapsuleCheckbox(
              text = stringResource(id = R.string.myEdit),
              checked = settings.includeSelf,
              onCheckedChange = { settings = settings.copy(includeSelf = it) }
            )
          }
          CapsuleCheckbox(
            text = stringResource(id = R.string.microEdit),
            checked = settings.includeMinor,
            onCheckedChange = { settings = settings.copy(includeMinor = it) }
          )
          CapsuleCheckbox(
            text = stringResource(id = R.string.robot),
            checked = settings.includeRobot,
            onCheckedChange = { settings = settings.copy(includeRobot = it) }
          )
          CapsuleCheckbox(
            text = stringResource(id = R.string.log),
            checked = settings.includeLog,
            onCheckedChange = { settings = settings.copy(includeLog = it) }
          )
        }
      }
    },
    onPrimaryButtonClick = {
      completableDeferred.complete(settings)
    },
    secondaryButton = ButtonConfig.cancelButton { completableDeferred.complete(null) },
    onDismiss = { completableDeferred.complete(null) },
  ))

  return completableDeferred.await()
}