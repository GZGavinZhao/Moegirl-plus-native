package com.moegirlviewer.screen.edit

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.moegirlviewer.R
import com.moegirlviewer.component.TopAppBarIcon
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.component.styled.TopAppBarTitle
import com.moegirlviewer.screen.edit.tabs.preview.EditScreenPreview
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.EditScreenWikitextEditor
import com.moegirlviewer.screen.edit.util.showSubmitDialogOfEdit
import com.moegirlviewer.theme.isUseDarkMode
import com.moegirlviewer.theme.isUsePureTheme
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.computeMd5
import com.moegirlviewer.util.imeBottomPadding
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(FlowPreview::class)
@InternalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun EditScreen(arguments: EditRouteArguments) {
  val model: EditScreenModel = hiltViewModel()

  SideEffect {
    model.routeArguments = arguments
    model.backupId = computeMd5(
      arguments.pageName + arguments.type.name + (arguments.section ?: "") + (arguments.preload ?: "")
    )
  }

  LaunchedEffect(true) {
    if (model.wikitextStatus == LoadStatus.INITIAL) {
      model.loadWikitext()
    }

    if (model.checkBackupFlag) {
      model.checkBackup()
      model.checkBackupFlag = false
    }
  }

  LaunchedEffect(model.pagerState.currentPage) {
    if (model.pagerState.currentPage == 1 && model.shouldReloadPreview) {
      model.shouldReloadPreview = false
      model.loadPreview()
    }
  }
  
//  BackHandler(true) {
//    scope.launch {
//      if (model.wikiEditorState.getTextContent() != model.originalWikiText) {
//        Globals.commonAlertDialog.show(CommonAlertDialogProps(
//          content = { StyledText(stringResource(id = R.string.editleaveHint)) },
//          secondaryButton = ButtonConfig.cancelButton(),
//          onPrimaryButtonClick = {
//            Globals.navController.popBackStack()
//          }
//        ))
//      } else {
//        Globals.navController.popBackStack()
//      }
//    }
//  }

  model.memoryStore.Provider {
    model.cachedWebViews.Provider {
      Scaffold(
        modifier = Modifier
          .imeBottomPadding(),
        topBar = {
          ComposedHeader(
            pageName = arguments.pageName,
            editType = arguments.type
          )
        }
      ) {
        HorizontalPager(
          count = 2,
          userScrollEnabled = false,
          state = model.pagerState,
        ) { currentIndex ->
          if (currentIndex == 0) {
            EditScreenWikitextEditor()
          } else {
            EditScreenPreview()
          }
        }
      }
    }
  }
}

@ExperimentalPagerApi
@Composable
private fun ComposedHeader(
  pageName: String,
  editType: EditType
) {
  val model: EditScreenModel = hiltViewModel()
  val configuration = LocalConfiguration.current
  val themeColors = MaterialTheme.colors

  LaunchedEffect(model.selectedTabIndex) {
    model.pagerState.animateScrollToPage(model.selectedTabIndex)
  }

  val titles = remember { listOf(
    Globals.context.getString(R.string.wikiText),
    Globals.context.getString(R.string.previewView)
  ) }

  val actionName = when(editType) {
    EditType.FULL -> stringResource(id = R.string.edit)
    EditType.SECTION -> stringResource(id = R.string.editSection)
    EditType.NEW_PAGE -> stringResource(id = R.string.create)
  }


  Surface(
    elevation = 5.dp
  ) {
    Column() {
      StyledTopAppBar(
        title = {
          TopAppBarTitle(
            text = "$actionName：$pageName",
            twoRows = true
          )
        },
        actions = {
          TopAppBarIcon(
            image = Icons.Filled.Search,
            onClick = {
              Globals.navController.navigate("search")
            }
          )

          TopAppBarIcon(
            image = Icons.Filled.Done,
            onClick = {
              showSubmitDialogOfEdit(model)
            }
          )
        }
      )

      TabRow(
        selectedTabIndex = model.selectedTabIndex,
        indicator = { tabPositions ->
          TabRowDefaults.Indicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[model.selectedTabIndex]),
            color = if (!isUsePureTheme() && !isUseDarkMode()) Color.White else themeColors.primaryVariant,
            height = 3.dp
          )
        }
      ) {
        titles.forEachIndexed { index, title ->
          Tab(
            text = {
              Text(
                text = title,
                color = Color.Unspecified
              )
            },
            selected = model.selectedTabIndex == index,
            onClick = { model.selectedTabIndex = index }
          )
        }
      }
    }
  }
}