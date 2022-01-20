package com.moegirlviewer.screen.comment.component.commentEditor

import com.moegirlviewer.R
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.*

fun CommentEditorController.showCommentEditor(
  coroutineScope: CoroutineScope,
  pageName: String,
  pageId: Int,
) = coroutineScope.launch {
  try {
    checkIsLoggedIn(Globals.context.getString(R.string.commentLoginHint))
    this@showCommentEditor.show(
      targetName = pageName,
      onSubmit = { inputVal ->
        coroutineScope.launch {
          if (inputVal.trim().isEmpty()) { return@launch }
          Globals.commonLoadingDialog.showText(Globals.context.getString(R.string.submitting))
          try {
            CommentStore.addComment(pageId, inputVal)
            toast(Globals.context.getString(R.string.published))
            this@showCommentEditor.hide()
            this@showCommentEditor.clearInputVal()
          } catch (e: Exception) {
            printRequestErr(e, "提交评论失败")
            toast(e.toString())
          } finally {
            Globals.commonLoadingDialog.hide()
          }
        }
      }
    )
  } catch (e: NotLoggedInException) {
    printPlainLog(e.toString())
  }
}

fun CommentEditorController.showReplyEditor(
  coroutineScope: CoroutineScope,
  targetUserName: String,
  pageId: Int,
  targetCommentId: String
) = coroutineScope.launch {
  try {
    checkIsLoggedIn(Globals.context.getString(R.string.replyLoginHint))
    this@showReplyEditor.show(
      isReply = true,
      targetName = targetUserName,
      onSubmit = { inputVal ->
        coroutineScope.launch {
          if (inputVal.trim().isEmpty()) { return@launch }
          Globals.commonLoadingDialog.showText(Globals.context.getString(R.string.submitting))
          try {
            CommentStore.addComment(pageId, inputVal, targetCommentId)
            toast(Globals.context.getString(R.string.published))
            this@showReplyEditor.hide()
            this@showReplyEditor.clearInputVal()
          } catch (e: Exception) {
            printRequestErr(e, "提交回复失败")
            toast(e.toString())
          } finally {
            Globals.commonLoadingDialog.hide()
          }
        }
      }
    )
  } catch (e: NotLoggedInException) {
    printPlainLog(e.toString())
  }
}