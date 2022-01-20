package com.moegirlviewer.screen.comment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.compable.OnSwipeLoading
import com.moegirlviewer.compable.RetainScroll
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.EmptyContent
import com.moegirlviewer.component.ScrollLoadListFooter
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.comment.component.commentEditor.showCommentEditor
import com.moegirlviewer.screen.comment.component.commentEditor.showReplyEditor
import com.moegirlviewer.screen.comment.component.commentEditor.useCommentEditor
import com.moegirlviewer.screen.comment.component.commentItem.CommentScreenCommentItem
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.store.PageComments
import com.moegirlviewer.ui.theme.background2
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun CommentScreen(
  arguments: CommentRouteArguments
) {
  val scope = rememberCoroutineScope()
  val model: CommentScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val configuration = LocalConfiguration.current
  val commentsFlow = CommentStore.getCommentsByPageId(arguments.pageId)
  val userNameOfCurrentAccount by AccountStore.userName.collectAsState(initial = "")
  val emptyPageComments = remember { PageComments() }

  val comments = commentsFlow.selector(
    initialFlowValue = emptyPageComments,
    selector = {
      object {
        val popularList = it.popular
        val commentList = it.commentTree
        val status = it.status
        val count = it.count
      }
    }
  )

  val (commentEditorController, CommentEditorHolder) = useCommentEditor()

  LaunchedEffect(true) {
    model.routeArguments = arguments
  }

  LaunchedEffect(comments.status) {
    model.swipeRefreshState.isRefreshing = comments.status == LoadStatus.INIT_LOADING
  }

  model.lazyListState.RetainScroll(comments.commentList.isNotEmpty())

  model.lazyListState.OnSwipeLoading {
    scope.launch {
      CommentStore.loadNext(arguments.pageId)
    }
  }

  model.memoryStore.Provider {
    Scaffold(
      backgroundColor = themeColors.background2,
      topBar = {
        ComposedHeader(
          title = arguments.pageName,
          onShowCommentEditor = {
            commentEditorController.showCommentEditor(model.coroutineScope, arguments.pageName, arguments.pageId)
          }
        )
      }
    ) {
      SwipeRefresh(
        state = model.swipeRefreshState,
        onRefresh = {
          scope.launch { CommentStore.loadNext(arguments.pageId, true) }
        },
        indicator = { state, trigger ->
          StyledSwipeRefreshIndicator(state, trigger)
        }
      ) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize(),
          state = model.lazyListState
        ) {
          if (comments.status != LoadStatus.EMPTY) {
            if (comments.popularList.isNotEmpty()) {
              item {
                Text(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  text = stringResource(id = R.string.hotComment),
                  color = themeColors.text.secondary,
                  fontSize = 17.sp,
                )
              }

              itemsIndexed(
                items = comments.popularList,
                key = { _, item -> "popular-${item.id}" }
              ) { _, item ->
                CommentScreenCommentItem(
                  isPopular = true,
                  commentData = item,
                  pageId = arguments.pageId,
//                  visibleDelButton = item.username == userNameOfCurrentAccount,
                )
              }
            }

            if (comments.commentList.isNotEmpty()) {
              item {
                Text(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  text = stringResource(id = R.string.commentTotal, comments.count),
                  color = themeColors.text.secondary,
                  fontSize = 17.sp,
                )
              }

              itemsIndexed(
                items = comments.commentList,
                key = { _, item -> item.id }
              ) { _, item ->
                CommentScreenCommentItem(
                  commentData = item,
                  pageId = arguments.pageId,
                  visibleReplyButton = true,
                  visibleReply = true,
                  visibleDelButton = item.username == userNameOfCurrentAccount,
                  onReplyButtonClick = {
                    commentEditorController.showReplyEditor(model.coroutineScope,
                      targetCommentId = item.id,
                      targetUserName = item.username,
                      pageId = arguments.pageId
                    )
                  },
                  onTargetUserNameClick = {}
                )
              }

              item {
                ScrollLoadListFooter(
                  status = comments.status,
                  onReload = { scope.launch { CommentStore.loadNext(arguments.pageId) } }
                )
              }
            }
          } else {
            item {
              EmptyContent()
            }
          }
        }
      }
    }

    CommentEditorHolder()
  }
}

@Composable
private fun ComposedHeader(
  title: String,
  onShowCommentEditor: () -> Unit
) {
  val model: CommentScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  StyledTopAppBar(
    navigationIcon = {
      BackButton()
    },
    title = {
      Text(
        text = "${stringResource(id = R.string.talk)}：${title}",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    actions = {
      AppHeaderIcon(
        image = Icons.Filled.AddComment,
        onClick = onShowCommentEditor
      )
    },
  )
}