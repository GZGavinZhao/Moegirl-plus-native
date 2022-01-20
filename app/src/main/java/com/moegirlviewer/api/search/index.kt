package com.moegirlviewer.api.search

import com.moegirlviewer.api.search.bean.SearchHintBean
import com.moegirlviewer.api.search.bean.SearchResultBean
import com.moegirlviewer.request.moeRequest
import com.moegirlviewer.util.MediaWikiNamespace

object SearchApi {
  suspend fun getHint(
    keyword: String,
    limit: Int = 20,
    namespace: MediaWikiNamespace? = null
  )  = moeRequest(
    entity = SearchHintBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["list"] = "search"
      this["srsearch"] = keyword
      this["srlimit"] = limit
      this["srwhat"] = "text"
      if (namespace != null) this["srnamespace"] = namespace.code
    }
  )

  suspend fun search(
    keyword: String,
    offset: Int
  ) = moeRequest(
    entity = SearchResultBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["list"] = "search"
      this["srsearch"] = keyword
      this["continue"] = "-||"
      this["sroffset"] = offset
      this["srprop"] = "timestamp|redirecttitle|snippet|categoriesnippet|sectiontitle|pageimages"
    }
  )
}