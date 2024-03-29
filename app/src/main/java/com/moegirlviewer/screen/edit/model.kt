package com.moegirlviewer.screen.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.moegirlviewer.R
import com.moegirlviewer.api.edit.EditApi
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.htmlWebView.HtmlWebView
import com.moegirlviewer.component.htmlWebView.HtmlWebViewContent
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.wikiEditor.WikiEditorState
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.request.MoeRequestTimeoutException
import com.moegirlviewer.room.backupRecord.BackupRecord
import com.moegirlviewer.room.backupRecord.BackupRecordType
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.article.ArticleScreenModel
import com.moegirlviewer.screen.compare.CompareTextRouteArguments
import com.moegirlviewer.screen.edit.tabs.wikitextEditor.component.QuickInsertText
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagerApi
class EditScreenModel @Inject constructor() : ViewModel() {
  val memoryStore = MemoryStore()
  val cachedWebViews = CachedWebViews()
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  lateinit var routeArguments: EditRouteArguments
  var selectedTabIndex by mutableStateOf(0)
  val pagerState = PagerState()

  val wikiEditorState = WikiEditorState()
  var originalWikiText = ""
  var wikitextStatus by mutableStateOf(LoadStatus.INITIAL)

  var previewHtml by mutableStateOf("")
  var previewStatus by mutableStateOf(LoadStatus.INITIAL)
  var shouldReloadPreview = true
  val isNewSection get() = routeArguments.section == "new"
  var backupId = ""
  var checkBackupFlag = true
  var baseDateISOForEdit: String? = null  // 基础维基代码版本的时间，传给编辑接口用于检查编辑冲突

  var quickInsertBarVisibleAllowed by mutableStateOf(true)

  suspend fun loadWikitext() {
    if (routeArguments.type == EditType.NEW_PAGE || isNewSection) {
      if (routeArguments.preload == null) {
        insertWikitext(QuickInsertText(
          text = "== ${Globals.context.getString(R.string.title)} ==",
          minusOffset = 3,
          selectionMinusOffset = 2
        ))

        wikitextStatus = LoadStatus.SUCCESS
      } else {
        wikitextStatus = LoadStatus.LOADING
        try {
          val res = EditApi.getWikitext(routeArguments.preload!!)
          val getPreloadContentRegex = Regex("""<includeonly>([\s\S]+)</includeonly>""")
          val preloadContent = getPreloadContentRegex.find(res.parse.wikitext._asterisk)?.groupValues?.get(1)
          wikiEditorState.setTextContent(preloadContent ?: "")
          wikitextStatus = LoadStatus.SUCCESS
        } catch (e: MoeRequestException) {
          printRequestErr(e, "加载预加载模版内容失败")
          wikitextStatus = LoadStatus.FAIL
        }
      }

      return
    }

    wikitextStatus = LoadStatus.LOADING

    try {
      baseDateISOForEdit = EditApi.getTimestampOfLastEdit(routeArguments.pageName)
    } catch (e: MoeRequestException) {
      printRequestErr(e, "编辑前获取最后编辑时间失败")
      wikitextStatus = LoadStatus.FAIL
    }

    try {
      val res = EditApi.getWikitext(routeArguments.pageName, routeArguments.section)
      wikiEditorState.setTextContent(res.parse.wikitext._asterisk)
      originalWikiText = res.parse.wikitext._asterisk
      wikitextStatus = LoadStatus.SUCCESS
    } catch(e: MoeRequestException) {
      printRequestErr(e, "编辑加载维基文本失败")
      wikitextStatus = LoadStatus.FAIL
    }
  }

  suspend fun loadPreview() {
    previewStatus = LoadStatus.LOADING
    try {
      val res = EditApi.getPreview(wikiEditorState.getTextContent(), routeArguments.pageName)
      previewHtml = res.parse.text._asterisk
      previewStatus = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "获取编辑预览失败")
      previewStatus = LoadStatus.FAIL
    }
  }

  suspend fun insertWikitext(insertText: QuickInsertText) {
    val cursorPosition = wikiEditorState.getPosition()
    val newCursorPosition = cursorPosition.copy(
      ch = cursorPosition.ch + insertText.text.length - insertText.minusOffset
    )

    wikiEditorState.insertTextAtCursor(insertText.text)
    if (insertText.selectionMinusOffset == 0) {
      wikiEditorState.setCursorPosition(newCursorPosition)
    } else {
      val selectionStartPosition = newCursorPosition.copy(
        ch = newCursorPosition.ch - 2
      )
      wikiEditorState.setSelection(selectionStartPosition, newCursorPosition)
    }
  }

  suspend fun makeBackup(content: String) {
    Globals.room.backupRecord().insertItem(BackupRecord(
      type = BackupRecordType.EDIT_CONTENT,
      backupId = backupId,
      content = content
    ))
  }

  suspend fun checkBackup() {
    val backupRoom = Globals.room.backupRecord()
    val backupRecord = backupRoom.getItem(BackupRecordType.EDIT_CONTENT, backupId).first() ?: return

    val lastEditDate = if (baseDateISOForEdit != null) {
      parseMoegirlNormalTimestamp(baseDateISOForEdit!!)
    } else {
      LocalDateTime.MIN
    }
    val isNewEdit = routeArguments.type == EditType.NEW_PAGE || isNewSection
    val isExpired = !isNewEdit && backupRecord.date.isBefore(lastEditDate)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val backupDateStr = dateFormatter.format(backupRecord.date)

    fun handleOnCheckRecovery() {
      fun restoreBackup() = coroutineScope.launch {
        wikiEditorState.setTextContent(backupRecord.content)
        toast(Globals.context.getString(R.string.backupRestored))
        Globals.commonAlertDialog.hide()
        backupRoom.deleteItem(backupRecord)
      }

      if (!isExpired) {
        restoreBackup()
        return
      }

      Globals.commonAlertDialog2.show(CommonAlertDialogProps(
        title = Globals.context.getString(R.string.attention),
        closeOnDismiss = false,
        content = { StyledText(stringResource(id = R.string.expiredBackupHint)) },
        primaryButtonText = Globals.context.getString(R.string.confirmRecovery),
        secondaryButton = ButtonConfig(Globals.context.getString(R.string.back)),
        onPrimaryButtonClick = { restoreBackup() },
        leftButton = ButtonConfig(
          text = Globals.context.getString(R.string.restoreToClipboard),
          onClick = {
            copyContentToClipboard(backupRecord.content)
            toast(Globals.context.getString(R.string.restoredToClipboard))
            Globals.commonAlertDialog.hide()
            Globals.commonAlertDialog2.hide()
          }
        )
      ))
    }

    fun handleOnCloseRecovery() = coroutineScope.launch {
      backupRoom.deleteItem(backupRecord)
      Globals.commonAlertDialog.hide()
    }

    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      title = Globals.context.getString(R.string.foundBackup),
      closeOnDismiss = false,
      closeOnAction = false,
      content = {
        StyledText(stringResource(id = R.string.hasBackupHint, backupDateStr))
      },
      primaryButtonText = Globals.context.getString(R.string.recovery),
      secondaryButton = ButtonConfig(
        text = Globals.context.getString(R.string.discard),
        onClick = { handleOnCloseRecovery() }
      ),
      onPrimaryButtonClick = { handleOnCheckRecovery() },
      leftButton = ButtonConfig(
        text = Globals.context.getString(R.string.viewDiff),
        onClick = {
          checkBackupFlag = true
          coroutineScope.launch {
            Globals.navController.navigate(CompareTextRouteArguments(
              formText = wikiEditorState.getTextContent(),
              toText = backupRecord.content
            ))
          }
        }
      )
    ))
  }

  /**
   * @return 是否关闭提交dialog
   */
  suspend fun submit(summary: String, minor: Boolean): Boolean {
    var fullSummary = ""
    var wikitext = wikiEditorState.getTextContent()
    val getTitleRegex = Regex("""^=+(.+?)=+\s*""")

    if (!isNewSection) {
      // 添加条目页章节信息，修改时允许没有标题
      val sectionName = getTitleRegex.find(wikitext)?.groupValues?.get(1)?.trim() ?:
        Globals.context.getString(R.string.noSpecifiedSection)
      fullSummary = "/*$sectionName*/$summary"
    } else {
      // 添加讨论页话题时，不允许没有标题
      if (!wikitext.contains(getTitleRegex)) {
        toast(Globals.context.getString(R.string.emptySectionHint))
        return false
      }
      fullSummary = getTitleRegex.find(wikitext)!!.groupValues[1].trim()
      // 在添加话题时，summary被视为标题，这时如果不把wiki代码中的标题替换掉将导致出现两个标题
      wikitext = wikitext.replaceFirst(getTitleRegex, "")
      if (wikitext == "") {
        toast(Globals.context.getString(R.string.emptyContentHint))
        return false
      }
    }

    // 提交编辑主体逻辑
    Globals.commonLoadingDialog.showText(Globals.context.getString(R.string.submitting) + "...")
    try {
      val res = EditApi.edit(
        pageName = routeArguments.pageName,
        section = routeArguments.section,
        content = wikitext,
        summary = fullSummary,
        minor = minor,
        baseDateISO = baseDateISOForEdit
      )

      if (res.edit.result == "Failure") {
        Globals.commonAlertDialog2.show(CommonAlertDialogProps(
          content = {
            val htmlWebViewRef = remember { Ref<HtmlWebViewRef>() }
            val configuration = LocalConfiguration.current

            LaunchedEffect(true) {
              htmlWebViewRef.value!!.updateContent {
                HtmlWebViewContent(
                  body = res.edit.warning!!
                )
              }
            }

            memoryStore.Provider {
              cachedWebViews.Provider {
                Column(
                  modifier = Modifier
                    .height((configuration.screenHeightDp * 0.6).dp),
                ) {
                  StyledText(stringResource(id = R.string.editFailedHint))
                  HtmlWebView(
                    ref = htmlWebViewRef
                  )
                }
              }
            }
          }
        ))

        return false
      }

      Globals.room.backupRecord().deleteItem(BackupRecord(
        type = BackupRecordType.EDIT_CONTENT,
        backupId = backupId,
        content = ""
      ))

      if (res.edit.new == null) {
        ArticleScreenModel.needReload = true
        Globals.navController.popBackStack()
      } else {
        Globals.navController.replace(ArticleRouteArguments(
          pageKey = PageNameKey(routeArguments.pageName)
        ))
      }

      toast(Globals.context.getString(R.string.edited))
      return true
    } catch(e: MoeRequestException) {
      val message = mapOf(
        "editconflict" to Globals.context.getString(R.string.editconflictHint),
        "protectedpage" to Globals.context.getString(R.string.protectedPageHint),
        "readonly" to Globals.context.getString(R.string.databaseReadonlyHint)
      )[e.code] ?: e.message!!

      Globals.commonAlertDialog2.showText(message)
      return true
    } catch (e: MoeRequestTimeoutException) {
      toast(Globals.context.getString(R.string.netErrToRetry))
      return false
    } finally {
      Globals.commonLoadingDialog.hide()
    }
  }

  override fun onCleared() {
    super.onCleared()
    cachedWebViews.destroyAllInstance()
  }
}