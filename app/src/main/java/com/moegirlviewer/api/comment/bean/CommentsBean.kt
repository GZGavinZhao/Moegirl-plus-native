package com.moegirlviewer.api.comment.bean

data class CommentsBean(
  val flowthread: Flowthread
) {
  data class Flowthread(
    val count: Int,
    val popular: List<Post>,
    val posts: List<Post>
  ) {
    open class Post(
      var id: String,
      var like: Int,
      var myatt: Int,
      var parentid: String,
      var text: String,
      var timestamp: Int,
      var userid: Int,
      var username: String
    )
  }
}