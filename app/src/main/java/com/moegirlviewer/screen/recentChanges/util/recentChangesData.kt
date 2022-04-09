package com.moegirlviewer.screen.recentChanges.util

import com.moegirlviewer.api.editingRecord.bean.RecentChangesBean
import com.moegirlviewer.api.watchList.bean.RecentChangesOfWatchList
import com.moegirlviewer.util.parseMoegirlNormalTimestamp
import java.time.LocalDateTime

// 这里的代码主要是为了统一“最近更改”和“监视列表下最近更改”的数据类型，以及合并同页面编辑，为数据添加编辑用户字段

fun processRecentChanges(list: List<RawRecentChanges>): List<List<RecentChanges>> {
  if (list.isEmpty()) return emptyList()
  return list
    .chunkedByDate()
    .map { it.withDetails().withUsers() }
}

private fun List<RawRecentChanges>.chunkedByDate(): List<List<RawRecentChanges>> {
  val dateChangePoints = this.foldIndexed(emptyList<Int>()) { index, result, item ->
    if (index == 0) return@foldIndexed listOf(0)
    val prevItemDate = parseMoegirlNormalTimestamp(this[index - 1].timestamp).toLocalDate()
    val itemDate = parseMoegirlNormalTimestamp(item.timestamp).toLocalDate()
    if (prevItemDate != itemDate)
      result + listOf(index) else
      result
  }

  return dateChangePoints
    .mapIndexed { index, point ->
      if (index == 0) emptyList() else this.subList(dateChangePoints[index - 1], point)
    }
    .drop(1)
    .plus(listOf(this.subList(dateChangePoints.last(), this.size)))
}

private fun List<RawRecentChanges>.withDetails(): List<RecentChanges> {
  return this.fold(mutableListOf()) { result, item ->
    if (result.all { it.title != item.title }) {
      result.add(RecentChanges(
        rawRecentChanges = item,
        details = mutableListOf(item),
      ))
    } else {
      result.first { it.title == item.title }.details.add(item)
    }

    result
  }
}

private fun List<RecentChanges>.withUsers(): List<RecentChanges> {
  for (item in this) {
    for (detailItem in item.details) {
      val foundUserIndex = item.users.indexOfFirst { it.name == detailItem.user }
      if (foundUserIndex != -1) {
        item.users[foundUserIndex].total++
      } else {
        item.users.add(EditUserOfChanges(
          name = detailItem.user,
          total = 1
        ))
      }
    }
  }

  return this
}

// 这个类的字段命名就按萌百接口返回数据的名字来命名了，方便查找
open class RawRecentChanges(
  val comment: String,
  val minor: String? = null,
  val newlen: Int,
  val ns: Int,
  val old_revid: Int,
  val oldlen: Int,
  val pageid: Int,
  val revid: Int,
  val timestamp: String,
  val title: String,
  val type: String,
  val user: String
) {
  constructor(data: RecentChangesBean.Query.Recentchange) : this(
    comment = data.comment,
    minor = data.minor,
    newlen = data.newlen,
    ns = data.ns,
    old_revid = data.old_revid,
    oldlen = data.oldlen,
    pageid = data.pageid,
    revid = data.revid,
    timestamp = data.timestamp,
    title = data.title,
    type = data.type,
    user = data.user,
  )

  constructor(data: RecentChangesOfWatchList.Query.Watchlist) : this(
    comment = data.comment,
    minor = data.minor,
    newlen = data.newlen,
    ns = data.ns,
    old_revid = data.old_revid,
    oldlen = data.oldlen,
    pageid = data.pageid,
    revid = data.revid,
    timestamp = data.timestamp,
    title = data.title,
    type = data.type,
    user = data.user,
  )
}

// 何箇所で処理しやすいためにMutableのを使っちゃった_(:з」∠)_，良い子はまねしないでね
data class EditUserOfChanges(
  val name: String,
  var total: Int,
)

class RecentChanges(
  rawRecentChanges: RawRecentChanges,
  val details: MutableList<RawRecentChanges> = mutableListOf(),
  val users: MutableList<EditUserOfChanges> = mutableListOf(),
) : RawRecentChanges(
  comment = rawRecentChanges.comment,
  minor = rawRecentChanges.minor,
  newlen = rawRecentChanges.newlen,
  ns = rawRecentChanges.ns,
  old_revid = rawRecentChanges.old_revid,
  oldlen = rawRecentChanges.oldlen,
  pageid = rawRecentChanges.pageid,
  revid = rawRecentChanges.revid,
  timestamp = rawRecentChanges.timestamp,
  title = rawRecentChanges.title,
  type = rawRecentChanges.type,
  user = rawRecentChanges.user,
)

