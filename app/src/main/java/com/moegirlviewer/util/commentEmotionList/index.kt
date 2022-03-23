package com.moegirlviewer.util.commentEmotionList

class CommentEmotionGroup(
  val iconUrl: String,
  val emotions: List<CommentEmotion>
)

class CommentEmotion(
  val imageUrl: String,
  val name: String,
)