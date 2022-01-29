package com.moegirlviewer.screen.settings.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moegirlviewer.R
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.store.SupportedLanguage
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//fun showLanguageSelectionDialog(
//  coroutineScope: CoroutineScope
//) {
//  var language: SupportedLanguage? = null
//  Globals.commonAlertDialog.show(CommonAlertDialogProps(
//    title = Globals.context.getString(R.string.changeLanguage),
//    content = {
//      var selectedLanguage by remember { mutableStateOf(SupportedLanguage.ZH_HANS) }
//
//      LaunchedEffect(true) {
//        selectedLanguage = SettingsStore.language.first()
//      }
//
//      SideEffect {
//        language = selectedLanguage
//      }
//
//      @Composable
//      fun Item(
//        title: String,
//        value: SupportedLanguage
//      ) {
//        Row(
//          modifier = Modifier
//            .clickable {
//              coroutineScope.launch { selectedLanguage = value }
//            }
//        ) {
//          RadioButton(
//            selected = selectedLanguage == value,
//            onClick = {
//              coroutineScope.launch { selectedLanguage = value }
//            }
//          )
//          StyledText(
//            text = title
//          )
//        }
//      }
//
//      Column(
//        modifier = Modifier
//          .selectableGroup()
//      ) {
//        Item(
//          title = "简体中文",
//          value = SupportedLanguage.ZH_HANS
//        )
//        Item(
//          title = "繁體中文",
//          value = SupportedLanguage.ZH_HANT
//        )
//      }
//    },
//
//    secondaryButton = ButtonConfig.cancelButton(),
//    onPrimaryButtonClick = {
//      coroutineScope.launch {
//        val currentLanguage = SettingsStore.language.first()
//        if (language != currentLanguage) {
//          toast(Globals.context.getString(R.string.changingLanguageRestartHint))
//          SettingsStore.setLanguage(language!!)
//        }
//      }
//    }
//  ))
//}