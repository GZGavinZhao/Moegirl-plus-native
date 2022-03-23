package com.moegirlviewer.util

import androidx.compose.ui.unit.Density
import com.moegirlviewer.api.comment.bean.CommentsBean
import com.moegirlviewer.component.nativeCommentContent.util.CommentElement
import com.moegirlviewer.component.nativeCommentContent.util.parseCommentHtml
import com.moegirlviewer.screen.comment.component.commentItem.maxCommentImageWidthReduction
import kotlin.math.floor

typealias Comment = CommentsBean.Flowthread.Post

class CommentTree(
  var data: List<CommentNode>
) {
  fun flattenReplyList(): CommentTree {
    return CommentTree(
      data = this.data.map {
        CommentNode(
          comment = it,
          children = it.children.flattenReplyList(),
          parsedText = it.parsedText
        )
      }
    )
  }

  companion object {
    fun formRawComments(rawComments: List<Comment>): CommentTree {
      val rootComments = rawComments.filter { it.parentid == "" }
      val treeData = rootComments.map { it.toRootCommentNode(rawComments) }
      return CommentTree(treeData)
    }

    fun empty(): CommentTree {
      return CommentTree(emptyList())
    }

    val CommentNode.replyList: List<ReplyCommentNode> get() {
      return this.children.map {
        val target = if (it.parentid == this.id) {
          this
        } else {
          this.children.first { childItem -> childItem.id == it.parentid }
        }

        ReplyCommentNode(it, target)
      }
    }

    fun List<CommentNode>.flattenReplyList(): List<CommentNode> {
      return this.fold(emptyList()) { result, item ->
        result + listOf(item) + item.children.flattenReplyList()
      }
    }
  }
}

open class CommentNode(
  comment: Comment,
  var children: List<CommentNode>,
  val parsedText: List<CommentElement> = parseComment(comment.text)
) : Comment(
  id = comment.id,
  like = comment.like,
  myatt = comment.myatt,
  parentid = comment.parentid,
  text = comment.text,
  timestamp = comment.timestamp,
  userid = comment.userid,
  username = comment.username
)

class ReplyCommentNode(
  commentNode: CommentNode,
  val target: CommentNode
) : CommentNode(
  comment = commentNode,
  children = commentNode.children,
  parsedText = commentNode.parsedText
)

private fun Comment.toRootCommentNode(commentsData: List<Comment>): CommentNode {
  fun traversal(parentComment: Comment): List<CommentNode> {
    val result = mutableListOf<CommentNode>()
    for (item in commentsData) {
      if (parentComment.id == item.parentid) {
        val commentNode = CommentNode(
          comment = item,
          children = traversal(item),
        )

        result.add(commentNode)
      }
    }

    return result
  }

  return CommentNode(
    comment = this,
    children = traversal(this),
  )
}

// 这里本来是写在视图组件的LaunchedEffect里，动态计算的，但是parseCommentHtml计算量大，渲染前计算有明显延迟，所以放到这里了
private fun parseComment(text: String): List<CommentElement> {
  // 屏幕宽度 - 评论item组件左边距(50) - 右边距(10)，组件见：screen/comment/component/commentItem
  val maxImageWidth =
    floor((Globals.activity.resources.displayMetrics.widthPixels / Globals.activity.resources.displayMetrics.density).toDouble()).toInt() - maxCommentImageWidthReduction

  return parseCommentHtml(
    html = text,
    density = Density(Globals.context),
    maxImageWidth = maxImageWidth
  )
}