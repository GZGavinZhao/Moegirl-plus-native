package com.moegirlviewer.screen.edit.tabs.wikitextEditor.util.tintWikitext

internal fun diffWikitext(
  before: String,
  after: String
): List<DiffBlock> {
  val (deletedString, addedString) = getDiff(before, after)
  val result = mutableListOf<DiffBlock>()
  val contentRegex = Regex("[^${Regex.escape(placeholder.toString())}]+")

  for (item in contentRegex.findAll(deletedString)) {
    result.add(DiffBlock(
      type = DiffType.MINUS,
      content = item.value,
      position = item.range.first
    ))
  }

  for (item in contentRegex.findAll(addedString)) {
    result.add(DiffBlock(
      type = DiffType.PLUS,
      content = item.value,
      position = item.range.first
    ))
  }

  return result
}

class DiffBlock(
  val type: DiffType,
  val content: String,
  val position: Int
)

enum class DiffType {
  PLUS,
  MINUS
}

private const val placeholder = '\t'

//获取两个字符串的差异，将相同部分设置为空格，返回的字符串数组为处理后的结果
private fun getDiff(a: String, b: String): Array<String> {
  var result: Array<String>? = null
  //选取长度较小的字符串用来穷举子串
  if (a.length < b.length) {
    result = getDiff(a, b, 0, a.length)
  } else {
    result = getDiff(b, a, 0, b.length)
    result = arrayOf(result[1], result[0])
  }
  return result
}

//将a的指定部分与b进行比较生成比对结果
private fun getDiff(a: String, b: String, start: Int, end: Int): Array<String> {
  var result = arrayOf(a, b)
  var len = result[0].length
  while (len > 0) {
    for (i in start until end - len + 1) {
      val sub = result[0].substring(i, i + len)
      var idx = -1
      if (result[1].indexOf(sub).also { idx = it } != -1) {
        result[0] = setEmpty(result[0], i, i + len)
        result[1] = setEmpty(result[1], idx, idx + len)
        if (i > 0) {
          //递归获取空白区域左边差异
          result = getDiff(result[0], result[1], 0, i)
        }
        if (i + len < end) {
          //递归获取空白区域右边差异
          result = getDiff(result[0], result[1], i + len, end)
        }
        len = 0 //退出while循环
        break
      }
    }
    len /= 2
  }
  return result
}

//将字符串s指定的区域设置成空格
private fun setEmpty(s: String, start: Int, end: Int): String {
  val array = s.toCharArray()
  for (i in start until end) {
    array[i] = placeholder
  }
  return String(array)
}