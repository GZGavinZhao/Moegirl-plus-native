package com.moegirlviewer.api.edit

import com.moegirlviewer.api.edit.bean.*
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.moeRequest
import com.moegirlviewer.util.moegirlNormalTimestampDateFormatter
import com.moegirlviewer.util.parseMoegirlNormalTimestamp
import com.moegirlviewer.util.printDebugLog

object EditApi {
  suspend fun getCsrfToken(): String {
    val res = moeRequest(
      entity = CsrfTokenBean::class.java,
      method = MoeRequestMethod.POST,
      params = mapOf(
        "action" to "query",
        "meta" to "tokens"
      )
    )

    return res.query.tokens.csrftoken
  }

  suspend fun getWikitext(
    pageName: String,
    section: String? = null
  ) = moeRequest(
    entity = WikitextBean::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "parse"
      this["page"] = pageName
      this["prop"] = "wikitext"
      if (section != null) this["section"] = section
    }
  )

  suspend fun getPreview(wikitext: String, pageName: String) = moeRequest(
    entity = EditPreviewBean::class.java,
    method = MoeRequestMethod.POST,
    params = mapOf<String, Any>(
      "action" to "parse",
      "text" to wikitext,
      "prop" to "text",
      "title" to pageName,
      "preview" to 1,
      "sectionpreview" to 1,
      "contentmodel" to "wikitext"
    )
  )

  suspend fun getTimestampOfLastEdit(pageName: String): String? {
    val res = moeRequest(
      entity = TimestampOfLastEdit::class.java,
      params = mapOf(
        "action" to "query",
        "prop" to "revisions",
        "titles" to pageName,
        "rvprop" to "timestamp",
        "rvlimit" to 1
      )
    )

    val revisions = res.query.pages.values.first().revisions
    return revisions?.first()?.timestamp
  }

  suspend fun edit(
    pageName: String,
    section: String? = null,
    content: String? = null,  // 非撤销操作必传
    summary: String,
    baseDateISO: String?,
    minor: Boolean = false,
    undoRevId: Int? = null // 传入该参数时，会执行撤销
  ): EditResultBean {
    val token = getCsrfToken()

    return moeRequest(
      entity = EditResultBean::class.java,
      method = MoeRequestMethod.POST,
      params = mutableMapOf<String, Any>().apply {
        this["action"] = "edit"
        this["tags"] = "Android App Edit"
        this["title"] = pageName
        this["summary"] = summary
        this["token"] = token
        if (content != null) this["text"] = content
        if (section != null) this["section"] = section
        this[if (minor) "minor" else "notminor"] = 1
        if (undoRevId != null) this["undo"] = undoRevId
        if (baseDateISO != null) this["basetimestamp"] = baseDateISO
      }
    )
  }
}