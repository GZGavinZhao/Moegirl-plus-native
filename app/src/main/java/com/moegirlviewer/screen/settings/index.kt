package com.moegirlviewer.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.settings.component.SettingsScreenItem
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CommonSettings
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.store.SupportedLanguage
import com.moegirlviewer.util.gotoUserPage
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
  val model: SettingsScreenModal = hiltViewModel()
  val scope = rememberCoroutineScope()
  val commonSettings by SettingsStore.common.getValue { this }.collectAsState(initial = CommonSettings())
//  val heimu by SettingsStore.heimu.collectAsState(initial = false)
//  val stopAudioOnLeave by SettingsStore.stopMediaOnLeave.collectAsState(initial = false)
//  val language by SettingsStore.language.collectAsState(initial = SupportedLanguage.ZH_HANS)
//  val syntaxHighlight by SettingsStore.syntaxHighlight.collectAsState(initial = true)
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)
  val themeColors = MaterialTheme.colors

  val switchColors = SwitchDefaults.colors(
    checkedThumbColor = themeColors.secondary,
    uncheckedTrackAlpha = 0.54f
  )

  fun setSettingItem(setter: CommonSettings.() -> Unit) {
    scope.launch {
      SettingsStore.common.setValue(setter)
    }
  }

  Scaffold(
    topBar = {
      StyledTopAppBar(
        title = {
          Text(stringResource(id = R.string.settings))
        }
      )
    }
  ) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
    ) {
      Title(R.string.article)
      SettingsScreenItem(
        title = stringResource(id = R.string.heimuSwitch),
        subtext = stringResource(id = R.string.heimuSwitchHelpText),
        onClick = {
          setSettingItem { this.heimu = !this.heimu }
        }
      ) {
        Switch(
          checked = commonSettings.heimu,
          colors = switchColors,
          onCheckedChange = {
            setSettingItem { this.heimu = it }
          }
        )
      }
      SettingsScreenItem(
        title = stringResource(id = R.string.stopAudioOnLeave),
        subtext = stringResource(id = R.string.stopAudioOnLeaveHelpText),
        onClick = {
          setSettingItem { this.stopMediaOnLeave = !this.stopMediaOnLeave }
        }
      ) {
        Switch(
          checked = commonSettings.stopMediaOnLeave,
          colors = switchColors,
          onCheckedChange = {
            setSettingItem { this.stopMediaOnLeave = it }
          }
        )
      }

      // 跟随系统，暂时不开放手动设置语言了
//      Title(R.string._interface)
//      SettingsScreenItem(
//        title = stringResource(id = R.string.changeLanguage),
//        onClick = {
//          showLanguageSelectionDialog(model.coroutineScope)
//        }
//      )

      Title(R.string.edit)
      SettingsScreenItem(
        title = stringResource(id = R.string.syntaxHighlight),
        subtext = stringResource(id = R.string.syntaxHighlightHelpText),
        onClick = {
          setSettingItem { this.syntaxHighlight = !this.syntaxHighlight }
        }
      ) {
        Switch(
          checked = commonSettings.syntaxHighlight,
          colors = switchColors,
          onCheckedChange = {
            setSettingItem { this.syntaxHighlight = it }
          }
        )
      }

      Title(R.string.account)
      SettingsScreenItem(
        title = stringResource(
          if (isLoggedIn) R.string.logout else R.string.login
        ),
        onClick = {
          scope.launch { model.toggleLoginStatus() }
        }
      )

      Title(R.string.about)
      SettingsScreenItem(
        title = stringResource(id = R.string.developer),
        onClick = {
          gotoUserPage("東東君")
        }
      ) {
        Text(
          text = "User:東東君",
          fontSize = 14.sp,
          color = themeColors.secondary,
          textDecoration = TextDecoration.Underline
        )
      }
    }
  }
}

@Composable
private fun Title(stringResourceId: Int) {
  val themeColors = MaterialTheme.colors

  Text(
    modifier = Modifier
      .padding(top = 10.dp, start = 10.dp, bottom = 5.dp),
    text = stringResource(id = stringResourceId),
    fontSize = 16.sp,
    color = themeColors.secondary
  )
}