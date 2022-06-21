package com.moegirlviewer.util

import android.util.Log

fun printRequestErr(error: Exception, message: String = "") {
  Log.e("--- 请求错误 ---", message)
  error.printStackTrace()
}

fun printPlainLog(message: String, error: Exception? = null) {
  Log.i("plainLog", message)
  error?.printStackTrace()
}

fun printDebugLog(vararg messages: Any?) {
  if (messages.size == 1) {
    Log.d("------------ debug -------------", messages[0].toString())
    return
  }

  Log.d("-------------------------------- debug divider --------------------------------", "")
  for ((index, item) in messages.withIndex()) {
    Log.d("------------ debug ------------- line:${index + 1} -----", item.toString())
  }
}