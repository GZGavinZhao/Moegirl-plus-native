package com.moegirlviewer.store

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.moegirlviewer.api.comment.CommentApi
import com.moegirlviewer.util.CommentNode
import com.moegirlviewer.util.CommentTree
import com.moegirlviewer.util.LoadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

data class PageComments(
  var popular: MutableList<CommentNode> = mutableListOf(),
  var commentTree: MutableList<CommentNode> = mutableListOf(),
  var offset: Int = 0,
  var count: Int = 0,
  var status: LoadStatus = LoadStatus.INITIAL
) {
  // Any.equals使用的是结构相等判断(也就是==)，这里为了便于更新要使用引用相等
  override fun equals(other: Any?) = this === other

  fun getCommentById(
    commentId: String,
    popular: Boolean = false,
  ): CommentNode? {
    val willFindList = if (popular)
      this.popular else
      this.commentTree

    var foundItem = try {
      willFindList.first { it.id == commentId }
    } catch (e: NoSuchElementException) { null }

    if (foundItem != null || popular) return foundItem

    foundItem = try {
      this.commentTree
        .flatMap { it.children }
        .first { it.id == commentId }
    } catch (e: NoSuchElementException) { null }

    return foundItem
  }
}

object CommentStore {
  private val store = mutableMapOf<Int, MutableStateFlow<PageComments>>()

  fun getCommentsByPageId(pageId: Int): Flow<PageComments> {
    if (store[pageId] == null) store[pageId] = MutableStateFlow(PageComments())
    return store[pageId]!!
  }

  @Composable
  fun rememberGetCommentsStateById(pageId: Int) = remember(pageId) { getCommentsByPageId(pageId) }.collectAsState(initial = PageComments())

  suspend fun loadNext(pageId: Int, refresh: Boolean = false) {
     if (store[pageId] == null) store[pageId] = MutableStateFlow(PageComments())
     val currentPageComments = if (refresh) PageComments() else getCommentsByPageId(pageId).first()

     try {
       if (LoadStatus.isCannotLoad(currentPageComments.status)) { return }
       store[pageId]!!.update {
         it.copy(
           status = if (currentPageComments.status == LoadStatus.INITIAL)
           LoadStatus.INIT_LOADING else
           LoadStatus.LOADING
         )
       }

       val commentData = CommentApi.getComments(pageId, currentPageComments.offset)
       val commentCount = commentData.posts.filter { it.parentid == "" }.size
       val nextStatus = when {
         currentPageComments.commentTree.isEmpty() && commentData.posts.isEmpty() -> LoadStatus.EMPTY
         currentPageComments.offset + commentCount >= commentData.count -> LoadStatus.ALL_LOADED
         else -> LoadStatus.SUCCESS
       }

       // 萌百的评论数据是用parentId格式串起来的，首先要树化，然后在第二层展平(将所有回复放在评论的children字段)
       withContext(Dispatchers.Default) {
         val newCommentData = CommentTree.formRawComments(commentData.posts).flattenReplyList().data

         // 为数据带上请求时的offset
         val newCommentDataWithRequestOffset = newCommentData.map {
           CommentNodeWithRequestOffset(it, currentPageComments.offset)
         }

         store[pageId]!!.update {
           PageComments(
             popular = commentData.popular.map { CommentNode(it, emptyList()) }.toMutableList(),
             commentTree = (currentPageComments.commentTree + newCommentDataWithRequestOffset).toMutableList(),
             offset = currentPageComments.offset + commentCount,
             count = commentData.count,
             status = nextStatus
           )
         }
       }
     } catch (e: Exception) {
        e.printStackTrace()
        store[pageId]!!.update {
          it.copy(status = LoadStatus.FAIL)
        }
     }
   }

  suspend fun setLike(
    pageId: Int,
    commentId: String,
    like: Boolean = true
  ) {
    CommentApi.toggleLike(commentId, like)
    val currentPageComments = getCommentsByPageId(pageId).first()
    val foundItem = currentPageComments.getCommentById(commentId)
    val foundPopularItem = currentPageComments.getCommentById(commentId, true)

    foundItem!!.like += if (like) 1 else -1
    foundItem!!.myatt = if (like) 1 else 0

    if (foundPopularItem != null) {
      foundPopularItem.like += if (like) 1 else -1
      foundPopularItem.myatt = if (like) 1 else 0
    }

    store[pageId]!!.update { it.copy() }
  }

  suspend fun addComment(
    pageId: Int,
    content: String,
    commentId: String? = null
  ) {
    CommentApi.postComment(pageId, content, commentId)
    val currentPageComments = getCommentsByPageId(pageId).first()

    // 因为萌百的评论api没返回评论id，这里只好手动去查
    if (commentId == null) {
      // 如果发的是评论，获取最近10条评论，并找出新评论。
      // 当然这样是有缺陷的，如果从发评论到服务器响应之间新增评论超过10条，就会导致不准。{{黑幕|不过不用担心，你百是不可能这么火的}}
      val lastCommentList = CommentApi.getComments(pageId)
      val currentCommentIds = currentPageComments.commentTree.map { it.id }
      val newCommentList = withContext(Dispatchers.Default) {
        lastCommentList.posts
          .filter { it.parentid == "" && !currentCommentIds.contains(it.id) }
          .map { CommentNodeWithRequestOffset(
            commentNode = CommentNode(it, emptyList()),
            requestOffset = 0
          ) }
      }

      store[pageId]!!.update {
        it.copy(
          commentTree = (newCommentList + it.commentTree).toMutableList(),
          count = it.count + newCommentList.size,
          status = if (it.status == LoadStatus.EMPTY) LoadStatus.ALL_LOADED else it.status
        )
      }
    } else {
      // 如果发的是回复，先找出其根评论
      val parentComment = currentPageComments.commentTree.first {
        it.id == commentId ||
        it.children.any { it.id == commentId }
      } as CommentNodeWithRequestOffset

      // 用回复目标根评论上的requestOffset发请求，再找出回复目标数据，赋给当前渲染的评论数据，实现更新回复
      val newTargetCommentList = CommentApi.getComments(pageId, parentComment.requestOffset)
      val newTargetComment = withContext(Dispatchers.Default) {
        try {
          CommentTree.formRawComments(newTargetCommentList.posts).flattenReplyList().data
            .first { it.id == parentComment.id }
        } catch (e: NoSuchElementException) { null }
      }

      if (newTargetComment != null) {
        parentComment.children = newTargetComment.children.map {
          CommentNodeWithRequestOffset(it, parentComment.requestOffset)
        }
      }

      store[pageId]!!.update {
        it.copy(
          commentTree = it.commentTree.toMutableList(),
        )
      }
    }
  }

  suspend fun removeComment(
    pageId: Int,
    commentId: String,
    parentCommentId: String? = null
  ) {
    CommentApi.delComment(commentId)
    val currentPageComments = getCommentsByPageId(pageId).first()

    val foundItem = currentPageComments.getCommentById(commentId)!!
    if (foundItem.parentid == "") {
      store[pageId]!!.update {
        val newCommentTree = it.commentTree.filter { it.id != foundItem.id }.toMutableList()
        it.copy(
          popular = it.popular.filter { it.id != foundItem.id }.toMutableList(),
          commentTree = newCommentTree,
          status = if (newCommentTree.isEmpty()) LoadStatus.EMPTY else it.status,
          count = it.count - 1
        )
      }
    } else {
      // 如果是回复，则要找到其父评论，并收集其所有子回复，删除本身和其子回复
      val foundRootComment = currentPageComments.getCommentById(parentCommentId!!)!!
      val childrenCommentIdList = mutableListOf(commentId)

      fun collectChildrenCommentId(idList: List<String>) {
        val oldListSize = childrenCommentIdList.size
        val resultIdList = foundRootComment.children
          .filter { idList.contains(it.parentid) }
          .map { it.id }
        childrenCommentIdList.addAll(resultIdList)
        if (oldListSize != childrenCommentIdList.size) {
          collectChildrenCommentId(resultIdList)
        }
      }

      collectChildrenCommentId(listOf(commentId))
      foundRootComment.children = foundRootComment.children
        .filter { !childrenCommentIdList.contains(it.id) }

      store[pageId]!!.update {
        it.copy(
          popular = it.popular.filter { it.id != commentId }.toMutableList(),
          commentTree = it.commentTree.toMutableList(),
        )
      }
    }
  }
}

private class CommentNodeWithRequestOffset(
  val commentNode: CommentNode,
  val requestOffset: Int
) : CommentNode(
  comment = commentNode,
  children = commentNode.children,
  parsedText = commentNode.parsedText
)