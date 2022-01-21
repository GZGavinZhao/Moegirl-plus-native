package com.moegirlviewer.screen.comment.component.commentItem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.comment.CommentApi
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.nativeCommentContent.NativeCommentContent
import com.moegirlviewer.component.nativeCommentContent.util.CommentInlineContent
import com.moegirlviewer.component.nativeCommentContent.util.CommentText
import com.moegirlviewer.screen.commentReply.CommentReplyRouteArguments
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.store.PageComments
import com.moegirlviewer.ui.theme.text
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
    } catch (e: Exception) {
      printRequestErr(e, "点赞操作失败")
      toast(e.toString())
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
        Text(message)
      },
      secondaryButton = ButtonConfig.cancelButton(),
      onPrimaryButtonClick = {
        Globals.commonLoadingDialog.show()
        try {
          scope.launch {
            CommentStore.removeComment(pageId, commentData.id, parentCommentId)
            toast(Globals.context.getString(if (isReply) R.string.replyDeleted else R.string.commentDeleted))
          }
        } catch (e: Exception) {
          printRequestErr(e, "删除评论失败")
          toast(e.toString())
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
        Text(message)
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
        } catch (e: Exception) {
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
          .padding(vertical = 10.dp, horizontal = 15.dp)
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
            .offset(x = (-5).dp, 5.dp)
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
    Image(
      modifier = Modifier
        .padding(end = 10.dp)
        .size(40.dp)
        .clip(CircleShape)
        .noRippleClickable { gotoUserPage(userName) },
      painter = rememberImagePainter(Constants.avatarUrl + userName),
      contentDescription = null
    )

    Column() {
      Text(
        modifier = Modifier
          .noRippleClickable { gotoUserPage(userName) },
        text = userName,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontSize = 15.sp
      )

      Text(
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
  val density = LocalDensity.current
  val currentPageCommentsFlow = remember { CommentStore.getCommentsByPageId(pageId) }
  var targetUserNameTextWidth by rememberSaveable { mutableStateOf(0f) }
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

  val replyTargetContent = remember(commentData, targetUserNameTextWidth) {
    if (visibleReplyTarget)  {
      val replyCommentData = commentData as ReplyCommentNode

      listOf(
        CommentText("回复 "),
        CommentInlineContent(
          id = "targetUserName",
          content = InlineTextContent(
            placeholder = Placeholder(
              // 这里有问题，由于用户名文字需要点击，评论内容又需要显示图片，但是clickableText又不支持inlineContent
              // 导致只能用inlineContent的形式显示文字，这样就需要手动设置文字宽度，而不同手机不同字体等情况比较复杂，这里使用一个透明组件在渲染完成后取值再赋给这里的方式
              // 缺点是还是没法用户名超过一行后不换行的问题，而且做法太不优雅
              width = targetUserNameTextWidth.sp,
              height = 1.25.em,
              placeholderVerticalAlign = PlaceholderVerticalAlign.Top
            )
          ) {
            Text(
              modifier = Modifier
                .noRippleClickable { onTargetUserNameClick?.invoke(replyCommentData.target.id) },
              text = replyCommentData.target.username,
              color = themeColors.secondary,
            )
          }
        ),
        CommentText("：")
      )
    } else null
  }

  Box() {
    // 用于计算回复目标用户名文字宽度的组件，获取到宽度后就隐藏
    if (visibleReplyTarget && targetUserNameTextWidth == 0f) {
      Text(
        modifier = Modifier
          .visibility(false)
          .onGloballyPositioned {
            targetUserNameTextWidth = density.run { it.size.width.toSp().value }
          },
        fontSize = 15.sp,
        text = (commentData as ReplyCommentNode).target.username,
      )
    }

    Column(
      modifier = Modifier
        .padding(start = 50.dp)
    ) {
      NativeCommentContent(
        modifier = Modifier
          .padding(top = 5.dp),
        commentElements = commentData.parsedText,
        prefixContents = replyTargetContent ?: emptyList()
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

          Text(
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

            Text(
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

          Text(
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
//  val isDarkTheme = isSystemInDarkTheme()
  val isDarkTheme = false
  val themeColors = MaterialTheme.colors

  Column(
    modifier = Modifier
      .padding(top = 10.dp, start = 50.dp, end = 25.dp, bottom = 5.dp)
      .fillMaxWidth()
      .background(if (isDarkTheme) themeColors.background else Color(0xffededed))
      .padding(10.dp)
  ) {
    for (item in replyList.take(3)) {
      Text(
        modifier = Modifier
          .padding(bottom = 2.dp),
        fontSize = 13.sp,
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

    Text(
      modifier = Modifier
        .padding(top = 3.dp)
        .noRippleClickable {
          Globals.navController.navigate(
            "commentReply", CommentReplyRouteArguments(
              pageId = pageId,
              commentId = commentData.id
            )
          )
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