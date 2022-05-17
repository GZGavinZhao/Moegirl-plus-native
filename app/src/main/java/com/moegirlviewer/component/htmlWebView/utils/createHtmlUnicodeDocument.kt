package com.moegirlviewer.component.htmlWebView.utils

import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.toUnicodeForInjectScriptInWebView
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

suspend fun createHtmlUnicodeDocument(
  body: String,
  title: String? = "Document",
  injectedStyles: List<String> = emptyList(),
  injectedScripts: List<String> = emptyList(),
  injectedAssetFiles: List<String> = emptyList(),
  injectedFilePaths: List<String> = emptyList(),
): String {
  val injectedStyleTagsStr = injectedStyles.joinToString("\n") { "<style>$it</style>" }
  val injectedScriptTagsStr = injectedScripts.joinToString("\n") { "<script>$it</script>" }

  val injectedCssFileTagsStr = injectedFilePaths
    .filter { it.contains(Regex("""\.css${'$'}""")) }
    .joinToString("\n") { "<link rel=\"stylesheet\" type=\"text/css\" href=\"$it\">" }

  val injectedJsFileTagsStr = injectedFilePaths
    .filter { it.contains(Regex("""\.js${'$'}""")) }
    .joinToString("\n") { "<script src=\"$it\"></script>" }

  val injectedCssFiles = coroutineScope {
    injectedAssetFiles
      .filter { it.contains(Regex("""\.css${'$'}""")) }
      .map { async { getContentUnicodeOfCachedAssetsFile(it) } }
      .map { it.await() }
      .joinToString(breakLineUnicode) { styleTagPairUnicode.first + it + styleTagPairUnicode.second }
  }
  val injectedJsFiles = coroutineScope {
    injectedAssetFiles.filter { it.contains(Regex("""\.js${'$'}""")) }
      .map { async { getContentUnicodeOfCachedAssetsFile(it) } }
      .map { it.await() }
      .joinToString(breakLineUnicode) { scriptTagPairUnicode.first + it + scriptTagPairUnicode.second }
  }

  val unicodeDocumentChunks = withContext(Dispatchers.Default) {
    listOf(
      """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <meta http-equiv="X-UA-Compatible" content="ie=edge">
        <title>$title</title>
        $injectedCssFileTagsStr
      """.trimIndent(),
      """
        $injectedStyleTagsStr
        </head>
        <body>$body</body>
      """.trimIndent(),
      """
      $injectedJsFileTagsStr
      $injectedScriptTagsStr
      </html>  
    """.trimIndent()
    )
      .map { async { it.toUnicodeForInjectScriptInWebView() } }
      .map { it.await() }
  }

  return unicodeDocumentChunks.toMutableList()
    .apply {
      add(1, injectedCssFiles)
      add(3, injectedJsFiles)
    }
    .joinToString("")
//  return """
//    <!DOCTYPE html>
//    <html lang="en">
//    <head>
//    <meta charset="UTF-8">
//    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
//    <meta http-equiv="X-UA-Compatible" content="ie=edge">
//    <title>$title</title>
//    $injectedCssFileTagsStr
//    $injectedStyleTagsStr
//    </head>
//    <body>$body</body>
//    $injectedJsFileTagsStr
//    $injectedScriptTagsStr
//    </html>
//  """.trimIndent()
}

private val breakLineUnicode = "\\u000a"

private val styleTagPairUnicode =
  "\\u003c\\u0073\\u0074\\u0079\\u006c\\u0065\\u003e" to
  "\\u003c\\u002f\\u0073\\u0074\\u0079\\u006c\\u0065\\u003e"

private val scriptTagPairUnicode =
  "\\u003c\\u0073\\u0063\\u0072\\u0069\\u0070\\u0074\\u003e" to
  "\\u003c\\u002f\\u0073\\u0063\\u0072\\u0069\\u0070\\u0074\\u003e"

private val injectedAssetsFileCache = mutableMapOf<String, String>()
private suspend fun getContentUnicodeOfCachedAssetsFile(filePath: String): String {
  return injectedAssetsFileCache[filePath] ?: run {
    val fileContent = withContext(Dispatchers.IO) {
      val inputStream = Globals.context.assets.open(filePath)
      BufferedReader(InputStreamReader(inputStream))
        .lines().parallel().collect(Collectors.joining("\n"))
    }

    withContext(Dispatchers.Default) {
      fileContent
        .toUnicodeForInjectScriptInWebView()
        .also { injectedAssetsFileCache[filePath] = it }
    }
  }
}