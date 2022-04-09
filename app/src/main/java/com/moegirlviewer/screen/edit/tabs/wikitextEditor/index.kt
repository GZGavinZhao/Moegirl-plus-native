package com.moegirlviewer.screen.edit.tabs.wikitextEditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.wikiEditor.WikiEditor
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.component.QuickInsertBar
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

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

