package com.moegirlviewer.api.edit.bean

import com.google.gson.annotations.SerializedName

data class EditPreviewBean(
  val parse: Parse
) {
  data class Parse(
    val pageid: Int,
    val text: Text,
    val title: String
  ) {
    data class Text(
      @SerializedName("*")
      val _asterisk: String
    )
  }
}