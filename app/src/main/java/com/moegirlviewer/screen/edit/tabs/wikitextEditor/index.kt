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
import com.moegirlviewer.R
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.component.QuickInsertBar
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext.TintedWikitext
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import kotlin.system.measureTimeMillis

@InternalCoroutinesApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@Composable
fun EditScreenWikitextEditor() {
  val model: EditScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  var textFieldValue by remember { mutableStateOf(model.wikitextTextFieldValue) }
  val tintedWikitext = remember { InitRef(TintedWikitext(textFieldValue.text)) }
  var visibleQuickInsertBar by remember { mutableStateOf(false) }
  var syntaxHighlight by remember { mutableStateOf(false) }
  // 将value转为flow，主要是为了要flow的防抖功能，用于更新备份
  val backupFlow = remember { MutableStateFlow(model.wikitextTextFieldValue.text) }


  LaunchedEffect(true) {
    syntaxHighlight = SettingsStore.common.getValue { this.syntaxHighlight }.first()
  }

  LaunchedEffect(model.wikitextTextFieldValue.text.isNotEmpty()) {
    if (model.wikitextTextFieldValue.text.isNotEmpty()) {
      tintedWikitext.value = withContext(Dispatchers.Default) {
        tintedWikitext.value.update(model.wikitextTextFieldValue.text, model.wikitextTextFieldValue.selection.end)
      }
      textFieldValue = model.wikitextTextFieldValue.copy(
        annotatedString = tintedWikitext.value.annotatedString
      )
    }
  }

  if (syntaxHighlight) {
    LaunchedEffect(model.wikitextTextFieldValue) {
      // 高亮性能优化暂时研究不明白了，先这样吧
      val consumingTime = measureTimeMillis {
//        tintedWikitext.value = withContext(Dispatchers.Default) {
//          tintedWikitext.value.hackedUpdate(model.wikitextTextFieldValue.text, model.wikitextTextFieldValue.selection.end)
//        }
//        textFieldValue = model.wikitextTextFieldValue.copy(
//          annotatedString = tintedWikitext.value.annotatedString
//        )

        coroutineScope {
          tintedWikitext.value = withContext(Dispatchers.Default) {
            tintedWikitext.value.update(model.wikitextTextFieldValue.text, model.wikitextTextFieldValue.selection.end)
          }
          textFieldValue = model.wikitextTextFieldValue.copy(
            annotatedString = tintedWikitext.value.annotatedString
          )
        }
      }

      if (consumingTime > 1000 && !isDebugEnv()) {
        toast(Globals.context.getString(R.string.codeHighlightTimeoutHint))
        syntaxHighlight = false
      }
    }
  }

  LaunchedEffect(model.wikitextTextFieldValue.text) {
    model.shouldReloadPreview = true
    backupFlow.emit(model.wikitextTextFieldValue.text)
  }

  LaunchedEffect(true) {
    backupFlow.debounce(1000).collect {
      if (it != "" && it != model.originalWikiText) model.makeBackup(it)
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
          fontSize = 16.sp,
          lineHeight = 18.sp
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
              modifier = Modifier
                .matchParentSize(),
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

