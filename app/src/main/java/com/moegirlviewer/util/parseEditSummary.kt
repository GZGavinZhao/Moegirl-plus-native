package com.moegirlviewer.util

fun parseEditSummary(summary: String): EditSummary {
  val sectionRegex = Regex("""\/\* (.+?) \*\/""")
  val body = summary.replaceFirst(sectionRegex, "")
  return EditSummary(
    body = if (body.trim() == "") null else body.trim(),
    section = sectionRegex.find(summary)?.groupValues?.get(1)
  )
}

class EditSummary(
  val body: String?,
  val section: String?
)