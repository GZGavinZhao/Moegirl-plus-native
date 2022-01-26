package com.moegirlviewer.api.comment

import com.moegirlviewer.api.comment.bean.CommentsBean
import com.moegirlviewer.api.comment.bean.ResultOfEditCommentBean
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.moeRequest

object CommentApi {
  suspend fun getComments(pageId: Int, offset: Int = 0): CommentsBean.Flowthread {
    val res = moeRequest(
      entity = CommentsBean::class.java,
      params = mapOf(
        "action" to "flowthread",
        "type" to "list",
        "pageid" to pageId,
        "offset" to offset
      )
    )

    return res.flowthread
  }

  suspend fun toggleLike(postId: String, like: Boolean) {
    val res = moeRequest(
      entity = ResultOfEditCommentBean::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "flowthread",
        "type" to if(like) "like" else "dislike",
        "postid" to postId
      )
    )

    if (res.error != null) throw Exception()
  }

  suspend fun report(postId: String) {
    val res = moeRequest(
      entity = ResultOfEditCommentBean::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "flowthread",
        "type" to "report",
        "postid" to postId
      )
    )

    if (res.error != null) throw Exception()
  }

  suspend fun delComment(postId: String) {
    val res = moeRequest(
      entity = ResultOfEditCommentBean::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "flowthread",
        "type" to "delete",
        "postid" to postId
      )
    )

    if (res.error != null) throw Exception()
  }

  suspend fun postComment(
    pageId: Int,
    content: String,
    postId: String? = null,
    useWikitext: Boolean = false,
  ) {
    val res = moeRequest(
      entity = ResultOfEditCommentBean::class.java,
      method = MoeRequestMethod.POST,
      params = mutableMapOf<String, Any>().apply {
        this["action"] = "flowthread"
        this["type"] = "post"
        this["pageid"] = pageId
        this["content"] = content
        if (postId != null) this["postid"] = postId
        if (useWikitext) this["wikitext"] = 1
      }
    )

    if (res.error != null) throw Exception()
  }
}