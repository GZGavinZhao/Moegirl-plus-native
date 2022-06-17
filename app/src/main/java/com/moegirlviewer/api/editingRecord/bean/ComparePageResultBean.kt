package com.moegirlviewer.api.editingRecord.bean

import com.google.gson.annotations.SerializedName

data class ComparePageResultBean(
  val compare: Compare
) {
  data class Compare(
    @SerializedName("*")
    val _asterisk: String? = null,
    val diffsize: Int,
    val fromcomment: String,
    val fromuser: String,
    val fromuserid: Int,
    val prev: Int,
    val tocomment: String,
    val touser: String,
    val touserid: Int
  )
}