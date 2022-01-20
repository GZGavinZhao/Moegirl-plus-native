package com.moegirlviewer.util

import android.util.Log

fun printRequestErr(error: Exception, message: String = "") {
  Log.w("--- 请求错误 ---", message, error)
}

fun printPlainLog(message: String, error: Exception? = null) {
  Log.i("plainLog", message, error)
}