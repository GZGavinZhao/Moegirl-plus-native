package com.moegirlviewer.api.editingRecord

import com.moegirlviewer.api.editingRecord.bean.*
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.moeRequest

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
    pageName: String,
    continueKey: String? = null
  ) = moeRequest(
    entity = PageRevisionsBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "query"
      this["prop"] = "revisions"
      this["continue"] = "||"
      this["titles"] = pageName
      this["rvprop"] = "timestamp|user|comment|ids|flags|size"
      this["rvlimit"] = "10"
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
}