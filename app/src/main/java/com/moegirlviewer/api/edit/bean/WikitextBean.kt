package com.moegirlviewer.api.edit.bean

import com.google.gson.annotations.SerializedName

data class WikitextBean(
  val parse: Parse
) {
  data class Parse(
    val pageid: Int,
    val title: String,
    val wikitext: Wikitext
  ) {
    data class Wikitext(
      @SerializedName("*")
      val _asterisk: String
    )
  }
}