package com.moegirlviewer.api.page.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class PageContentResBean(
  val parse: Parse
) : Parcelable {
  @Parcelize
  data class Parse(
    val categories: List<Category>,
    val displaytitle: String,
    val images: List<String>,
    val pageid: Int,
    val sections: List<Section>,
    val templates: List<Template>,
    val text: Text,
    val title: String
  ) : Parcelable {
    @Parcelize
    data class Category(
      @SerializedName("*")
      val _asterisk: String,
      val sortkey: String,
      val hidden: String? = null
    ) : Parcelable

    @Parcelize
    data class Section(
      val anchor: String,
      val byteoffset: Int,
      val fromtitle: String,
      val index: String,
      val level: String,
      val line: String,
      val number: String,
      val toclevel: Int
    ) : Parcelable

    @Parcelize
    data class Template(
      @SerializedName("*")
      val _asterisk: String,
      val exists: String,
      val ns: Int
    ) : Parcelable

    @Parcelize
    data class Text(
      @SerializedName("*")
      val _asterisk: String
    ) : Parcelable
  }
}