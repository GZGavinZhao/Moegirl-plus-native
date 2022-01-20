package com.moegirlviewer.api.edit.bean

data class EditResultBean(
  val edit: Edit
) {
  open class Edit(
    val result: String,

    // 这些字段只有在编辑成功时才有
    val contentmodel: String? = null,
    val newrevid: Int? = null,
    val newtimestamp: String? = null,
    val oldrevid: Int? = null,
    val pageid: Int? = null,
    val title: String? = null,

    // 被拦截器拦截会有这些字段
    val abusefilter: Abusefilter? = null,
    val code: String? = null,
    val info: String? = null,
    val message: Message? = null,
    val warning: String? = null
  ) {
    data class Abusefilter(
      val actions: List<String>,
      val description: String,
      val id: Int
    )

    data class Message(
      val key: String,
      val params: List<Any>
    )
  }
}
