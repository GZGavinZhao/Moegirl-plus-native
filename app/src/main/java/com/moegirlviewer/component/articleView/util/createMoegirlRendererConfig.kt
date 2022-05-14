package com.moegirlviewer.component.articleView.util

import com.google.gson.Gson

fun createMoegirlRendererConfig(
  pageName: String? = null,
  language: String,
  site: String,
  enabledCategories: Boolean,
  categories: List<String>,
  enabledHeightObserver: Boolean,
  heimu: Boolean,
  nightMode: Boolean,
  addCopyright: Boolean
): String {
  val categoriesStr = Gson().toJson(categories)

  val heightObserverCodes = """
    moegirl.config.hostScrollMode.enabled = true
    moegirl.config.hostScrollMode.onResize = height => _postMessage('pageHeightChange', { value: height })
  """.trimIndent()

  val preProcessCodes = """
    (() => {
      const commonUrlRegex = ${if (site == "moegirl")
        """/https:\/\/img\.moegirl\.org\.cn\/common\//g""" else
        """/https:\/\/www\.hmoegirl\.com\/thumb\.php/g"""
      }
      const replaceToProxyUrl = (url) => '/commonRes/' + encodeURIComponent(url)
      ${'$'}('body')
        .find('source, img').each(function() {
          const src = ${'$'}(this).attr('src') || ''
          const srcset = ${'$'}(this).attr('srcset') || ''
          commonUrlRegex.lastIndex = 0
          const srcTestResult = commonUrlRegex.test(src)
          commonUrlRegex.lastIndex = 0
          const srcsetTestResult = commonUrlRegex.test(srcset)
          if (!srcTestResult && !srcsetTestResult) { return }
          if (src !== '') ${'$'}(this).attr('src', replaceToProxyUrl(src))
          if (srcset !== '') ${'$'}(this).attr('srcset', replaceToProxyUrl(srcset))
        })
    })()
  """.trimIndent()

  return """
    $preProcessCodes
    moegirl.data.pageName = ${Gson().toJson(pageName)}
    moegirl.data.language = '$language'
    moegirl.data.site = '$site'
    moegirl.config.heimu.${'$'}enabled = $heimu
    moegirl.config.addCopyright.enabled = $addCopyright
    moegirl.config.nightTheme.${'$'}enabled = $nightMode
  
    moegirl.config.link.onClick = (data) => _postMessage('link', data)
    moegirl.config.biliPlayer.onClick = (data) => _postMessage('biliPlayer', data)
    moegirl.config.biliPlayer.onLongPress = (data) => _postMessage('biliPlayerLongPress', data)
    moegirl.config.request.onRequested = (data) => _postMessage('request', data)
    moegirl.config.vibrate.onCalled = () => _postMessage('vibrate')
    moegirl.config.addCategories.enabled = $enabledCategories
    moegirl.config.addCategories.categories = $categoriesStr
    moegirl.config.dataCollector.catalogData = data => _postMessage('catalogData', { value: data })
    moegirl.config.poll.onPoll = (data) => _postMessage('poll', data)
    ${if (enabledHeightObserver) heightObserverCodes else ""}
    moegirl.init()
  """.trimIndent()
}