package com.moegirlviewer.screen.article.utils.injectedWebViewEvents

class InjectedWebViewEvents<T>(
  private val messageName: String,
  val messageHandler: (T) -> Unit,
)