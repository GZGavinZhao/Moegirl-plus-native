package com.moegirlviewer.api.editingRecord

import com.moegirlviewer.api.editingRecord.bean.*
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.moeRequest
import com.moegirlviewer.util.PageKey
import com.moegirlviewer.util.addQueryApiParamsByPageKey

object EditingRecordApi {
  suspend fun getRecentChanges(
    startISO: String,
    namespace: String? = null,
    excludeUser: String? = null,
    includeMinor: Boolean,
    includeRobot: Boolean,
    includeLog: Boolean,
    limit: Int
  ): RecentChangesBean {
    val showing = mutableListOf<String>()
    val type = mutableListOf("edit", "new")
    if (!includeMinor) showing.add("!minor")
    if (!includeRobot) showing.add("!bot")
    if (includeLog) type.add("log")


    return moeRequest(
      entity = RecentChangesBean::class.java,
      params = mutableMapOf<String, Any>().apply {
        this["action"] = "query"
        this["list"] = "recentchanges"
        this["rcend"] = startISO
        this["rcprop"] = "tags|comment|flags|user|title|timestamp|ids|sizes|redirect"
        this["rcshow"] = showing.joinToString("|")
        this["rclimit"] = limit
        this["rctype"] = type.joinToString("|")
        if (namespace != null) this["rcnamespace"] = namespace
        if (excludeUser != null) this["rcexcludeuser"] = excludeUser
      }
    )
  }

  suspend fun comparePage(
    fromTitle: String? = null,
    fromRev: Int? = null,
    toTitle: String? = null,
    toRev: Int? = null,
    fromText: String? = null,
    toText: String? = null
  ) = moeRequest(
    entity = ComparePageResult::class.java,
    method = MoeRequestMethod.POST,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "compare"
      this["prop"] = "diff|diffsize|rel|user|comment"
      if (fromTitle != null) this["fromtitle"] = fromTitle
      if (fromRev != null) this["fromrev"] = fromRev
      if (toTitle != null) this["totitle"] = toTitle
      if (toRev != null) this["torev"] = toRev
      if (fromText != null) this["fromtext"] = fromText
      if (toText != null) this["totext"] = toText
    }
  )

  suspend fun getPageRevisions(
    pageKey: PageKey,
    continueKey: String? = null,
    limit: Int = 10
  ) = moeRequest(
    entity = PageRevisionsBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["prop"] = "revisions"
      this["continue"] = "||"
      addQueryApiParamsByPageKey(pageKey)
      this["rvprop"] = "timestamp|user|comment|ids|flags|size"
      this["rvlimit"] = limit
      if (continueKey != null) this["rvcontinue"] = continueKey
    }
  )

  suspend fun getUserContribution(
    userName: String,
    startISO: String,
    endISO: String,
    continueKey: String?
  ) = moeRequest(
    entity = UserContributionBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["list"] = "usercontribs"
      this["ucprop"] = "ids|title|timestamp|comment|sizediff|flags|tags"
      this["ucuser"] = userName
      this["uclimit"] = 10
      this["ucstart"] = startISO
      this["ucend"] = endISO
      this["continue"] = "||"
      if (continueKey != null) this["uccontinue"] = continueKey
    }
  )
  
//  suspend fun getNewPages(
//    continueKey: String?
//  ) = moeRequest(
//    entity = NewPagesBean::class.java,
//    params = mutableMapOf<String, Any>().apply {
//      this["action"] = "query"
//      this["format"] = "json"
//      this["prop"] = "pageimages|extracts"
//      this["continue"] = "grccontinue||"
//      this["generator"] = "recentchanges"
//      this["piprop"] = "thumbnail"
//      this["pithumbsize"] = "500"
//      this["pilimit"] = "20"
//      this["exsentences"] = "10"
//      this["exlimit"] = "20"
//      this["exintro"] = 1
//      this["explaintext"] = 1
//      this["grcnamespace"] = "0"
//      this["grclimit"] = "20"
//      this["grctype"] = "new"
//      this["grcshow"] = "!redirect"
//      if (continueKey != null) this["grccontinue"] = continueKey
//    }
//  )

  suspend fun getNewPages(
    continueKey: String? = null
  ) = moeRequest(
    entity = NewPagesBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["list"] = "recentchanges"
      this["rcnamespace"] = 0
      this["rcshow"] = "!redirect"
      this["rclimit"] = "20"
      this["rctype"] = "new"
      if (continueKey != null) this["rccontinue"] = continueKey
    }
  )
}