package com.moegirlviewer.api.search

import com.moegirlviewer.api.search.bean.SearchHintBean
import com.moegirlviewer.api.search.bean.SearchResultBean
import com.moegirlviewer.request.moeRequest
import com.moegirlviewer.util.MediaWikiNamespace

object SearchApi {
  suspend fun getHint(
    keyword: String,
    limit: Int = 50,
//    offset: Int = 0,    // 不知为何设置这个后，返回的搜索结果就会为空
    namespace: MediaWikiNamespace? = null
  )  = moeRequest(
    entity = SearchHintBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["list"] = "prefixsearch"
      this["pssearch"] = keyword
      this["pslimit"] = limit
//      this["psoffset"] = offset
      if (namespace != null) this["psnamespace"] = namespace.code

      this["gpssearch"] = keyword
      this["gpslimit"] = limit
      this["pithumbsize"] = 500
//      this["gpsoffset"] = offset
      this["prop"] = "pageimages"
      this["generator"] = "prefixsearch"
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
      this["sroffset"] = offset
      this["srprop"] = "timestamp|redirecttitle|snippet|categoriesnippet|sectiontitle|pageimages"
      this["srwhat"] = "text"

      this["gsrsearch"] = keyword
      this["gsroffset"] = offset
      this["pithumbsize"] = 500
      this["prop"] = "pageimages"
      this["generator"] = "search"
      this["gsrwhat"] = "text"
    }
  )
}