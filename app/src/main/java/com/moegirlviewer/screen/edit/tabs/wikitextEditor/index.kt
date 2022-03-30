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
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.wikiEditor.WikiEditor
import com.moegirlviewer.component.wikiEditor.WikiEditorState
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
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  var visibleQuickInsertBar by remember { mutableStateOf(false) }

  fun makeBackup() = scope.launch {
    val currentContent = model.wikiEditorState.getTextContent()
    if (currentContent != "" && currentContent != model.originalWikiText) model.makeBackup(currentContent)
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
      WikiEditor(
        modifier = Modifier
          .weight(1f),
        state = model.wikiEditorState,
        onTextChange = {
          model.shouldReloadPreview = true
          makeBackup()
        }
      )

      if (visibleQuickInsertBar && model.quickInsertBarVisibleAllowed) {
        QuickInsertBar(
          onClickItem = {
            scope.launch { model.insertWikitext(it) }
          }
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

