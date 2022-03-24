package com.moegirlviewer.api.watchList

import com.moegirlviewer.Constants
import com.moegirlviewer.api.watchList.bean.RawWatchListBean
import com.moegirlviewer.api.watchList.bean.RecentChangesOfWatchList
import com.moegirlviewer.api.watchList.bean.WatchTokenBean
import com.moegirlviewer.request.MoeRequestMethod
import com.moegirlviewer.request.MoeRequestWikiException
import com.moegirlviewer.request.moeOkHttpClient
import com.moegirlviewer.request.moeRequest
import com.moegirlviewer.request.util.bodyContentHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup

object WatchListApi {
  suspend fun setWatchStatus(pageName: String, watch: Boolean) = moeRequest(
    method = MoeRequestMethod.POST,
    entity = Any::class.java,
    params = mutableMapOf<String, Any>().apply {
      this["action"] = "watch"
      if (!watch) this["unwatch"] = 1
      this["titles"] = pageName
      this["redirects"] = 1
      this["token"] = getWatchToken()
    }
  )

  suspend fun getRecentChanges(
    startISO: String,
    includeMinor: Boolean,
    includeRobot: Boolean,
    includeLog: Boolean,
    limit: Int
  ): RecentChangesOfWatchList {
    val showing = mutableListOf<String>()
    val type = mutableListOf("edit", "new")
    if (!includeMinor) showing.add("!minor")
    if (!includeRobot) showing.add("!bot")
    if (includeLog) type.add("log")

    return moeRequest(
      entity = RecentChangesOfWatchList::class.java,
      params = mapOf(
        "action" to "query",
        "format" to "json",
        "list" to "watchlist",
        "wlend" to startISO,
        "wllimit" to limit,
        "wlshow" to showing.joinToString("|"),
        "wlallrev" to 1,
        "wlprop" to "flags|user|comment|timestamp|ids|title|sizes"
      )
    )
  }

  suspend fun getRawWatchList(): List<String> {
    val rawHtml = moeRequest(
      entity = String::class.java,
      baseUrl = Constants.mainUrl + "/Special:%E7%BC%96%E8%BE%91%E7%9B%91%E8%A7%86%E5%88%97%E8%A1%A8/raw",
    )

    val htmlDoc = Jsoup.parse(rawHtml)
    return htmlDoc.getElementById("mw-input-wpTitles")!!.text().split("\n")
  }
}

private suspend fun getWatchToken(): String {
  val res = moeRequest(
    entity = WatchTokenBean::class.java,
    params = mapOf(
      "action" to "query",
      "meta" to "tokens",
      "type" to "watch"
    )
  )

  return res.query.tokens.watchtoken
}