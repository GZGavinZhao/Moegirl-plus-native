package com.moegirlviewer.component.htmlWebView.utils

fun createHtmlDocument(
  body: String,
  title: String? = "Document",
  injectedStyles: List<String> = emptyList(),
  injectedScripts: List<String> = emptyList(),
  injectedFiles: List<String> = emptyList(),
): String {
  val injectedStyleTagsStr = injectedStyles.joinToString("\n") { "<style>$it</style>" }
  val injectedScriptTagsStr = injectedScripts.joinToString("\n") { "<script>$it</script>" }

  val injectedCssFileTagsStr = injectedFiles
    .filter { it.contains(Regex("""\.css${'$'}""")) }
    .joinToString("\n") { "<link rel=\"stylesheet\" type=\"text/css\" href=\"$it\">" }

  val injectedJsFileTagsStr = injectedFiles
    .filter { it.contains(Regex("""\.js${'$'}""")) }
    .joinToString("\n") { "<script src=\"$it\"></script>" }

  return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>$title</title>
    $injectedCssFileTagsStr
    $injectedStyleTagsStr
    </head>
    <body>$body</body>
    $injectedJsFileTagsStr
    $injectedScriptTagsStr
    </html>   
  """.trimIndent()
}