package com.moegirlviewer.screen.commentReply

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.comment.component.commentEditor.showReplyEditor
import com.moegirlviewer.screen.comment.component.commentEditor.useCommentEditor
import com.moegirlviewer.screen.comment.component.commentItem.CommentScreenCommentItem
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.store.PageComments
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.CommentTree.Companion.replyList
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.selector
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun CommentReplyScreen(
  arguments: CommentReplyRouteArguments
) {
  val scope = rememberCoroutineScope()
  val model: CommentReplyScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val commentsFlow = remember { CommentStore.getCommentsByPageId(arguments.pageId) }
  val emptyPageComments = remember { PageComments() }
  val comment = commentsFlow.selector(
    initialFlowValue = emptyPageComments,
    selector = { it.getCommentById(arguments.commentId) }
  )
  val replyList = commentsFlow.selector(
    initialFlowValue = emptyPageComments,
    selector = { it.getCommentById(arguments.commentId)?.replyList?.reversed() ?: emptyList() }
  )
  val userNameOfCurrentAccount by AccountStore.userName.collectAsState(initial = "")

  val (commentEditorController, CommentEditorHolder) = useCommentEditor()

  LaunchedEffect(true) {
    model.routeArguments = arguments
  }

  if (model.itemRefs.size != replyList.size) {
    for (item in replyList) model.itemRefs[item.id] = Ref()
  }

  model.memoryStore.Provider {
    Scaffold(
      backgroundColor = themeColors.background2,
      topBar = {
        ComposedHeader(
          userName = comment?.username ?: "",
          onReply = {
            commentEditorController.showReplyEditor(model.coroutineScope,
              targetCommentId = arguments.commentId,
              targetUserName = comment!!.username,
              pageId = arguments.pageId
            )
          }
        )
      }
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize(),
        state = if (replyList.isNotEmpty()) model.lazyListState else rememberLazyListState()
      ) {
        if (comment != null) {
          item {
            CommentScreenCommentItem(
              commentData = comment,
              pageId = arguments.pageId,
              isReply = true,
            )
          }
        }

        if (replyList.isNotEmpty()) {
          item {
            StyledText(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              text = stringResource(id = R.string.replyTotal, replyList.size),
              color = themeColors.text.secondary,
              fontSize = 17.sp,
            )
          }
        }

        itemsIndexed(
          items = replyList,
          key = { _, item -> item.id }
        ) { _, item ->
          CommentScreenCommentItem(
            commentData = item,
            pageId = arguments.pageId,
            visibleDelButton = userNameOfCurrentAccount == item.username,
            visibleReplyButton = true,
            parentCommentId = arguments.commentId,
            onReplyButtonClick = {
              commentEditorController.showReplyEditor(model.coroutineScope,
                targetCommentId = item.id,
                targetUserName = item.username,
                pageId = arguments.pageId,
              )
            },
            ref = model.itemRefs[item.id],
            onTargetUserNameClick = { targetCommentId ->
              scope.launch {
                model.lazyListState.animateScrollToItem(replyList.indexOfFirst { it.id == targetCommentId })
                model.itemRefs[targetCommentId]!!.value!!.show()
              }
            },
          )
        }

        item {
          ScrollLoadListFooter(
            status = LoadStatus.ALL_LOADED,
            onReload = {}
          )
        }
      }
    }

    CommentEditorHolder()
  }
}

@Composable
private fun ComposedHeader(
  userName: String,
  onReply: () -> Unit,
) {
  val themeColors = MaterialTheme.colors

  StyledTopAppBar(
    navigationIcon = {
      BackButton()
    },
    title = {
      StyledText(
        text = "${stringResource(id = R.string.reply)}：$userName",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = themeColors.onPrimary
      )
    },
    actions = {
      AppHeaderIcon(
        image = Icons.Filled.Reply,
        onClick = { onReply() }
      )
    },
  )
}