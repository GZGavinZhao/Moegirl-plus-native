package com.moegirlviewer.util

enum class MediaWikiNamespace(val code: Int) {
  MAIN(0),
  MAIN_TALK(1),
  USER(2),
  USER_TALK(3),
  PROJECT(4),
  PROJECT_TALK(5),
  FILE(6),
  FILE_TALK(7),
  MEDIA_WIKI(8),
  MEDIA_WIKI_TALK(9),
  TEMPLATE(10),
  TEMPLATE_TALK(11),
  HELP(12),
  HELP_TALK(13),
  CATEGORY(14),
  CATEGORY_TALK(15),

  WIDGET(274),
  WIDGET_TALK(275),
  TIMED_TEXT(710),
  TIMED_TEXT_TALK(711),
  MODULE(828),
  MODULE_TALK(829);

  companion object {
    fun getNamespaceByCode(code: Int): MediaWikiNamespace {
      return values().find { it.code == code } ?: throw Exception("没有code为'${code}'命名空间")
    }

    fun isTalkPage(code: Int): Boolean {
      return getNamespaceByCode(code).name.matches(Regex("""_TALK${'$'}"""))
    }
  }
}