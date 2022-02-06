package com.moegirlviewer.screen.edit.tabs.wikitextEditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.component.QuickInsertBar
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.NospzGothicMoeFamily
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

@InternalCoroutinesApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun EditScreenWikitextEditor() {
  val model: EditScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  var textFieldValue by remember(model.wikitextTextFieldValue) { mutableStateOf(
    model.wikitextTextFieldValue.copy(
      annotatedString = tintWikitext(model.wikitextTextFieldValue.text)
    )
  ) }
  var visibleQuickInsertBar by remember { mutableStateOf(false) }
  val syntaxHighlight by SettingsStore.common.getValue { this.syntaxHighlight }.collectAsState(
    initial = true
  )
  // 将value转为flow，主要是为了要flow的防抖功能，用于更新备份
  val backupFlow = remember { MutableStateFlow(model.wikitextTextFieldValue.text) }

//  if (syntaxHighlight) {
//    LaunchedEffect(model.wikitextTextFieldValue) {
//      textFieldValue = model.wikitextTextFieldValue.copy(
//        annotatedString = withContext(Dispatchers.Default) { linearTintWikitext(model.wikitextTextFieldValue.text) }
//      )
//    }
//  }

  LaunchedEffect(model.wikitextTextFieldValue.text) {
    model.shouldReloadPreview = true
    backupFlow.emit(model.wikitextTextFieldValue.text)
  }

  LaunchedEffect(true) {
    backupFlow.debounce(1000).collect {
      if (it !== model.originalWikiText) model.makeBackup(it)
    }
  }

  DisposableEffect(true) {
    val unregister = KeyboardVisibilityEvent.registerEventListener(Globals.activity) {
      visibleQuickInsertBar = it
    }

    onDispose {
      unregister.unregister()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
    ) {
      PlainTextField(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .focusRequester(model.focusRequester),
        value = if (syntaxHighlight) textFieldValue else model.wikitextTextFieldValue,
        textStyle = TextStyle.Default.copy(
          fontSize = 16.sp
        ),
        onValueChange = {
          model.wikitextTextFieldValue = it
        },
        decorationBox = { self ->
          Box(
            modifier = Modifier
              .padding(horizontal = 3.dp)
          ) { self() }
        }
      )

      if (visibleQuickInsertBar && model.quickInsertBarVisibleAllowed) {
        QuickInsertBar(
          onClickItem = { model.insertWikitext(it) }
        )
      }
    }

    if (model.wikitextStatus != LoadStatus.SUCCESS) {
      Center(
        modifier = Modifier
          .background(themeColors.background)
      ) {
        when(model.wikitextStatus) {
          LoadStatus.FAIL -> {
            ReloadButton(
              onClick = {
                scope.launch { model.loadWikitext() }
              }
            )
          }

          else -> StyledCircularProgressIndicator()
        }
      }
    }
  }
}

