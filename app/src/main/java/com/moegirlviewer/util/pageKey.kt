package com.moegirlviewer.util

sealed class PageKey

class PageNameKey(vararg val pageName: String) : PageKey()
class PageIdKey(vararg val pageId: Int): PageKey()

fun MutableMap<String, Any>.addQueryApiParamsByPageKey(pageKey: PageKey) = when(pageKey) {
  is PageNameKey -> this["titles"] = pageKey.pageName.joinToString("|")
  is PageIdKey -> this["pageids"] = pageKey.pageId.joinToString("|")
}

val PageKey.triedPageNameOrNull get() = if (this is PageNameKey) pageName.firstOrNull() else null
val PageKey.triedPageIdOrNull get() = if (this is PageIdKey) pageId.firstOrNull() else null