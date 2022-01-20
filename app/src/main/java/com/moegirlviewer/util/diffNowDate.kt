package com.moegirlviewer.util

import com.moegirlviewer.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.floor

fun diffNowDate(date: LocalDateTime): String {
  val nowTimeMillis = System.currentTimeMillis()
  val diff = nowTimeMillis - date.toEpochMilli()

  val yearWord = Globals.context.getString(R.string.year)
  val monthWord = Globals.context.getString(R.string.month)
  val dateWord = Globals.context.getString(R.string.date)
  val agoWord = Globals.context.getString(R.string.ago)
  val daysAgoWord = Globals.context.getString(R.string.days) + agoWord
  val hoursAgoWord = Globals.context.getString(R.string.hours) + agoWord
  val minutesAgoWord = Globals.context.getString(R.string.minutes) + agoWord
  val secondsAgoWord = Globals.context.getString(R.string.seconds) + agoWord

  val fullDateFormat = DateTimeFormatter.ofPattern("yyyy${yearWord}MM${monthWord}dd${dateWord}")
  val thisYearDateFormat = DateTimeFormatter.ofPattern("MM${monthWord}dd${dateWord}")
  val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

  var needFullDate = false
  val result = when {
    diff < 60 * 1000 -> floor((diff / 1000).toDouble()).toInt().toString() + secondsAgoWord
    diff < 60 * 60 * 1000 -> floor((diff / 60 / 1000).toDouble()).toInt().toString() + minutesAgoWord
    diff < 60 * 60 * 24 * 1000 -> floor((diff / 60 / 60 / 1000).toDouble()).toInt().toString() + hoursAgoWord
    diff < (60).toLong() * 60 * 24 * 30 * 1000 -> {
      needFullDate = true
      floor((diff / 60 / 60 / 24 / 1000).toDouble()).toInt().toString() + daysAgoWord
    }
    else -> {
      needFullDate = true
      (if (date.year == LocalDate.now().year) thisYearDateFormat else fullDateFormat)
        .format(date)
    }
  }

  return if (needFullDate)
    "$result ${timeFormat.format(date)}" else
    result
}