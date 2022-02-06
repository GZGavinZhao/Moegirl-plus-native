package com.moegirlviewer.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import androidx.compose.runtime.Composable
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.moegirlviewer.Constants
import com.moegirlviewer.DataSource
import com.moegirlviewer.R
import com.moegirlviewer.screen.article.ArticleRouteArguments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toHexString
import java.math.BigInteger
import java.security.MessageDigest
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Target(AnnotationTarget.CLASS)
annotation class ProguardIgnore

fun isMoegirl() = Constants.source == DataSource.MOEGIRL

fun <T> isMoegirl(ifIsTrue: T, ifIsFalse: T): T {
  return if (isMoegirl()) ifIsTrue else ifIsFalse
}

fun isDebugEnv() = (Globals.context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE) != 0

fun openHttpUrl(url: String) {
  val intent = Intent(Intent.ACTION_VIEW)
  intent.data = Uri.parse(url)
  Globals.activity.startActivity(intent)
}

// 主要用于在向webView注入脚本之前，将脚本内容转义
suspend fun String.toUnicodeForInjectScriptInWebView(): String {
  val text = this
  return withContext(Dispatchers.Default) {
    fun encode(char: Char) = "\\u" + char.code.toHexString().padStart(4, '0')
    text.toCharArray().joinToString("") { encode(it) }
  }
}

fun shareText(content: String) {
  val intent = Intent(Intent.ACTION_SEND)
  intent.type = "text/plain"
  intent.putExtra(Intent.EXTRA_TEXT, content)
  val shareText = Globals.context.getString(R.string.share)
  Globals.activity.startActivity(Intent.createChooser(intent, shareText))
}

@Composable
fun Int.toDp(): Dp {
  return (this / LocalDensity.current.density).dp
}

@Composable
fun Float.toDp(): Dp {
  return (this / LocalDensity.current.density).dp
}

class InitRef<T>(var value: T)

fun getTextFromHtml(html: String): String {
  return html
    .replace(Regex("""(<.+?>|<\\/.+?>)"""), "")
    .replace(Regex("&(.+?);")) {
      mapOf(
        "gt" to ">",
        "lt" to "<",
        "amp" to "&"
      )[it.groupValues[1]] ?: it.groupValues[0]
    }
    .trim()
}

fun copyContentToClipboard(content: String) {
  val cm = Globals.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clipData = ClipData.newPlainText("", content)
  cm.setPrimaryClip(clipData)
}

fun vibrate() {
  val vibrator = Globals.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
  vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
}

fun gotoArticlePage(pageName: String) {
  Globals.navController.navigate(ArticleRouteArguments(
    pageName = pageName
  ))
}

fun gotoUserPage(userName: String) {
  gotoArticlePage("User:$userName")
}

fun Char.isHalfWidth(): Boolean {
  return this.toString().contains(Regex("[\\u0000-\\u00ff]"))
}

fun Long.toLocalDateTime(): LocalDateTime {
  return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Long.toLocalDate(): LocalDate {
  return this.toLocalDateTime().toLocalDate()
}

fun LocalDateTime.toEpochMilli(): Long {
  return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun LocalDate.toEpochMilli(): Long {
  return this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

// 有的手机返回的语言代码不规范，只好这么判断
fun isSimplifiedChineseEnv() = listOf("zh-CN", "zh-SG", "zh-Hans-CN", "zh-Hans-SG").contains(Locale.getDefault().toLanguageTag()) ||
  Locale.getDefault().displayCountry == "中国"

fun computeMd5(content: String): String {
  val md = MessageDigest.getInstance("MD5")
  return BigInteger(1, md.digest(content.toByteArray())).toString(16).padStart(32, '0')
}

val moegirlNormalTimestampDateFormatter: DateTimeFormatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

// 萌百通常api返回这种格式的时间戳，评论api返回秒数时间戳(注意不是毫秒)
fun parseMoegirlNormalTimestamp(timestamp: String): LocalDateTime {
  // 这里不知道为什么DateTimeFormatter.ISO_INSTANT不行，看文档明明应该可以的
  return LocalDateTime.parse(timestamp, moegirlNormalTimestampDateFormatter).plusHours(8)
}

fun Date.toLocalDate(): LocalDate {
  val instant = this.toInstant()
  val zoneId = ZoneId.systemDefault()
  return instant.atZone(zoneId).toLocalDate()
}

fun LocalDate.toDate(): Date {
  val zoneId = ZoneId.systemDefault()
  val zdt: ZonedDateTime = this.atStartOfDay(zoneId)
  return Date.from(zdt.toInstant())
}

fun TextGeometricTransform.Companion.Italic() = TextGeometricTransform(
  skewX = -0.3f
)