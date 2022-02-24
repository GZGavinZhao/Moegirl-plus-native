package com.moegirlviewer.api.account.bean

data class ThankResultBean(
  val result: Result
) {
  data class Result(
    val recipient: String,
    val success: Int
  )
}