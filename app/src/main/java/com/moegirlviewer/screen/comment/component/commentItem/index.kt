package com.moegirlviewer.screen.comment.component.commentItem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.comment.CommentApi
import com.moegirlviewer.component.UserAvatar
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.nativeCommentContent.NativeCommentContent
import com.moegirlviewer.component.nativeCommentContent.util.CommentCustomAnnotatedText
import com.moegirlviewer.component.nativeCommentContent.util.CommentText
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.commentReply.CommentReplyRouteArguments
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.store.PageComments
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import com.moegirlviewer.util.CommentTree.Companion.replyList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class CommentScreenCommentItemRef(
  val show: suspend () -> Unit
)

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun CommentScreenCommentItem(
  commentData: CommentNode,
  pageId: Int,
  parentCommentId: String? = null,  // 注意这个不是萌百接口返回数据中的parentid字段，而是经过第二级扁平化后的父id(也就是根评论id)
  isReply: Boolean = false,
  isPopular: Boolean = false,
  visibleReply: Boolean = false,
  visibleReplyButton: Boolean = false,
  visibleDelButton: Boolean = false,
  ref: Ref<CommentScreenCommentItemRef>? = null,
  onReplyButtonClick: ((commentId: String) -> Unit)? = null,
  onTargetUserNameClick: ((targetCommentId: String) -> Unit)? = null,
) {
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val replyList = remember(commentData.children) { commentData.replyList }

  var maskAlpha by remember { mutableStateOf(0f) }
  val animatedMaskAlpha by animateFloatAsState(
    targetValue = maskAlpha,
    animationSpec = tween(
      durationMillis = 250
    )
  )

  fun showMask() = scope.launch {
    maskAlpha = 0.5f
    delay(250)
    maskAlpha = 0f
  }

  fun toggleLike() = scope.launch {
    try {
      checkIsLoggedIn(Globals.context.getString(R.string.likeLoginHint))
      val isLiked = commentData.myatt == 1
      Globals.commonLoadingDialog.show()
      CommentStore.setLike(pageId, commentData.id, !isLiked)
    } catch (e: MoeRequestException) {
      printRequestErr(e, "点赞操作失败")
      toast(e.message)
    } catch (e: NotLoggedInException) {
      printPlainLog(e.message ?: "", e)
    } finally {
      Globals.commonLoadingDialog.hide()
    }
  }

  fun delComment() {
    val commentWord = Globals.context.getString(R.string.comment)
    val replyWord = Globals.context.getString(R.string.reply)
    val message = Globals.context.getString(R.string.delCommentHint, if (isReply) replyWord else commentWord)

    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(message)
      },
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        Globals.commonLoadingDialog.show()
        try {
          scope.launch {
            CommentStore.removeComment(pageId, commentData.id, parentCommentId)
            toast(Globals.context.getString(if (isReply) R.string.replyDeleted else R.string.commentDeleted))
          }
        } catch (e: MoeRequestException) {
          printRequestErr(e, "删除评论失败")
          toast(e.message)
        } finally {
          Globals.commonLoadingDialog.hide()
        }
      }
    ))
  }

  fun reportComment() {
    val commentWord = Globals.context.getString(R.string.comment)
    val replyWord = Globals.context.getString(R.string.reply)
    val message = Globals.context.getString(R.string.reportHint, if (isReply) replyWord else commentWord)

    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(message)
      },
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        Globals.commonLoadingDialog.show()
        try {
          scope.launch {
            CommentApi.report(commentData.id)
            Globals.commonAlertDialog.hide()
            Globals.commonAlertDialog.showText(Globals.context.getString(R.string.reoprtedHint))
          }
        } catch (e: MoeRequestException) {
          printRequestErr(e, "举报评论失败")
          toast(Globals.context.getString(R.string.netErr))
        } finally {
          Globals.commonLoadingDialog.hide()
        }
      }
    ))
  }

  SideEffect {
    ref?.value = CommentScreenCommentItemRef(
      show = { showMask() }
    )
  }

  Surface(
    modifier = Modifier
      .padding(bottom = 1.dp)
      .fillMaxWidth()
      .background(themeColors.surface)
      // 这里有问题，会被内部的clickableText挡住，应该用pointerInput手动检测手势，设置由父组件优先处理手势，如果不满足长按条件则不消耗手势，放行给子组件处理
      .combinedClickable(
        enabled = true,
        onClick = {},
        onLongClick = {
          copyContentToClipboard(getTextFromHtml(commentData.text.eraseTargetUserName()))
          vibrate()
          toast(Globals.context.getString(R.string.commentContentCopiedHint))
        }
      )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      Spacer(modifier = Modifier
        .matchParentSize()
        .background(themeColors.secondary.copy(alpha = animatedMaskAlpha))
      )

      Column(
        modifier = Modifier
          .padding(vertical = 10.dp, horizontal = 15.dp),
        verticalArrangement = Arrangement.Center
      ) {
        ComposedHeader(
          userName = commentData.username,
          dateTime = (commentData.timestamp.toLong() * 1000).toLocalDateTime(),
        )

        ComposedCommentContent(
          commentData = commentData,
          visibleReplyButton = visibleReplyButton,
          visibleReplyTarget = commentData.parentid != "" &&
            parentCommentId != null &&
            commentData.parentid != parentCommentId,
          isPopular = isPopular,
          pageId = pageId,
          onLike = { toggleLike() },
          onReply = { onReplyButtonClick?.invoke(commentData.id) },
          onReport = { reportComment() },
          onTargetUserNameClick = onTargetUserNameClick
        )

        if (visibleReply && replyList.isNotEmpty()) {
          ComposedCommentReply(
            commentData = commentData,
            replyList = replyList,
            pageId = pageId
          )
        }
      }

      if (visibleDelButton) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .offset(x = (-10).dp, 10.dp)
            .zIndex(1f),
          contentAlignment = Alignment.TopEnd
        ) {
          Icon(
            modifier = Modifier
              .size(18.dp)
              .noRippleClickable { delComment() },
            imageVector = Icons.Filled.Clear,
            contentDescription = null,
            tint = themeColors.text.tertiary
          )
        }
      }
    }
  }
}

@Composable
private fun ComposedHeader(
  userName: String,
  dateTime: LocalDateTime,
) {
  val themeColors = MaterialTheme.colors

  Row() {
    UserAvatar(
      modifier = Modifier
        .padding(end = 10.dp)
        .size(40.dp),
      userName = userName
    )

    Column() {
      StyledText(
        modifier = Modifier
          .noRippleClickable { gotoUserPage(userName) },
        text = userName,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontSize = 15.sp
      )

      StyledText(
        text = diffNowDate(dateTime),
        color = themeColors.text.secondary,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
private fun ComposedCommentContent(
  pageId: Int,
  isPopular: Boolean,
  commentData: CommentNode,
  visibleReplyTarget: Boolean,
  visibleReplyButton: Boolean,
  onLike: () -> Unit,
  onReply: () -> Unit,
  onReport: () -> Unit,
  onTargetUserNameClick: ((targetCommentId: String) -> Unit)?
) {
  val themeColors = MaterialTheme.colors
  val currentPageCommentsFlow = remember { CommentStore.getCommentsByPageId(pageId) }
  val likeNumber = currentPageCommentsFlow.selector(
    initialFlowValue = remember { PageComments() },
    selector = { it.getCommentById(commentData.id, isPopular)?.like ?: 0 }
  )
  val isMyLiked = currentPageCommentsFlow.selector(
    initialFlowValue = remember { PageComments() },
    selector = {
      it.getCommentById(commentData.id, isPopular)?.myatt == 1
    }
  )

  val annotationTagNameOfTargetUserName = "targetUserName"
  val replyTargetContent = remember(commentData) {
    if (visibleReplyTarget)  {
      val replyCommentData = commentData as ReplyCommentNode

      listOf(
        CommentText("回复 "),
        CommentCustomAnnotatedText(
          text = replyCommentData.target.username,
          tag = annotationTagNameOfTargetUserName,
          annotation = replyCommentData.target.id,
          textStyle = SpanStyle(
            color = themeColors.secondary
          )
        ),
        CommentText("：")
      )
    } else null
  }

  Box() {
    Column(
      modifier = Modifier
        .padding(top = 5.dp, start = 50.dp, end = 25.dp)
    ) {
      NativeCommentContent(
        commentElements = commentData.parsedText,
        prefixContents = replyTargetContent ?: emptyList(),
        linkedTextStyle = SpanStyle(
          color = themeColors.secondary,
          textDecoration = TextDecoration.Underline
        ),
        onAnnotatedTextClick = {
          if (it.tag == annotationTagNameOfTargetUserName) onTargetUserNameClick?.invoke(it.item)
        }
      )

      Row(
        modifier = Modifier
          .padding(top = 10.dp, end = 25.dp)
          .fillMaxWidth()
          .offset(y = (-1).dp),
      ) {
        // 点赞按钮
        Row(
          modifier = Modifier
            .offset(y = (-1).dp)
            .noRippleClickable { onLike() },
          verticalAlignment = Alignment.CenterVertically,
        ) {
          when {
            likeNumber == 0 -> Icon(
              modifier = Modifier
                .size(17.dp),
              imageVector = Icons.Outlined.ThumbUp,
              contentDescription = null,
              tint = themeColors.text.tertiary
            )
            likeNumber > 0 && !isMyLiked -> Icon(
              modifier = Modifier
                .size(17.dp),
              imageVector = Icons.Outlined.ThumbUp,
              contentDescription = null,
              tint = themeColors.secondary
            )
            else -> Icon(
              modifier = Modifier
                .size(17.dp),
              imageVector = Icons.Filled.ThumbUp,
              contentDescription = null,
              tint = themeColors.secondary
            )
          }

          StyledText(
            modifier = Modifier
              .padding(start = 5.dp, top = 2.5.dp),
            text = likeNumber.toString(),
            color = if (likeNumber > 0)
              themeColors.secondary else
              themeColors.text.tertiary,
            fontSize = 13.sp
          )
        }

        // 回复按钮
        if (visibleReplyButton) {
          Row(
            modifier = Modifier
              .padding(start = 18.dp)
              .noRippleClickable { onReply() },
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              modifier = Modifier
                .size(18.dp),
              imageVector = ImageVector.vectorResource(id = R.drawable.reply),
              contentDescription = null,
              tint = themeColors.secondary
            )

            StyledText(
              modifier = Modifier
                .padding(start = 2.dp),
              text = stringResource(id = R.string.reply),
              color = themeColors.secondary,
              fontSize = 13.sp
            )
          }
        }

        // 举报按钮
        Row(
          modifier = Modifier
            .padding(start = 20.dp)
            .noRippleClickable { onReport() },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            modifier = Modifier
              .size(19.dp),
            imageVector = Icons.Filled.AssistantPhoto,
            tint = themeColors.text.tertiary,
            contentDescription = null
          )

          StyledText(
            modifier = Modifier
              .padding(start = 3.dp),
            text = stringResource(id = R.string.report),
            color = themeColors.text.tertiary,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
private fun ComposedCommentReply(
  pageId: Int,
  commentData: CommentNode,
  replyList: List<ReplyCommentNode>,
) {
  val themeColors = MaterialTheme.colors
  val isDarkTheme = !themeColors.isLight

  Column(
    modifier = Modifier
      .padding(top = 10.dp, start = 50.dp, end = 25.dp, bottom = 5.dp)
      .fillMaxWidth()
      .background(if (isDarkTheme) themeColors.background else Color(0xffededed))
      .padding(10.dp)
  ) {
    for (item in replyList.take(3)) {
      StyledText(
        modifier = Modifier
          .padding(bottom = 2.dp),
        fontSize = 14.sp,
        text = buildAnnotatedString {
          withStyle(SpanStyle(color = themeColors.secondary)) {
            append(item.username)
          }

          if (item.target.id != commentData.id) {
            append(" ${stringResource(id = R.string.reply)} ")
            withStyle(SpanStyle(color = themeColors.secondary)) {
              append(item.target.username)
            }
          }

          append("：")
          append(getTextFromHtml(item.text))
        }
      )
    }

    StyledText(
      modifier = Modifier
        .padding(top = 3.dp)
        .noRippleClickable {
          Globals.navController.navigate(CommentReplyRouteArguments(
            pageId = pageId,
            commentId = commentData.id
          ))
        },
      text = stringResource(id = R.string.replyTotal, replyList.size) + " >",
      color = themeColors.secondary,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

// 评论回复超过3层后回复前会出现用户名链接，需要去掉
private fun String.eraseTargetUserName(): String {
  return this.replace(Regex("""^@<a[\s\S]+?</a>"""), "")
}