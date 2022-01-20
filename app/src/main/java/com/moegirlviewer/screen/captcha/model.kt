package com.moegirlviewer.screen.captcha

import androidx.compose.material.Text
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.htmlWebView.HtmlWebViewContent
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.screen.home.HomeScreenModel
import com.moegirlviewer.util.CachedWebViews
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.printRequestErr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject

@HiltViewModel
class CaptchaScreenModel @Inject constructor() : ViewModel() {
  val cacheWebViews = CachedWebViews()
  val memoryStore = MemoryStore()
  val htmlWebViewRef = Ref<HtmlWebViewRef>()
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  lateinit var routeArguments: CaptchaRouteArguments

  fun initWebViewContent() {
    val htmlDoc = Jsoup.parse(routeArguments.captchaHtml)
    // 移除最后一个执行脚本，使用自定义的逻辑
    htmlDoc.head().select("script").last()?.remove()

    val viewPortMetaTag = Element("meta")
    viewPortMetaTag.attr("name", "viewport")
    viewPortMetaTag.attr("content", "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no")
    htmlDoc.head().appendChild(viewPortMetaTag)

    val scriptTag = Element("script")
    scriptTag.html("""
      window._postMessage = function(type, data) {
        window._NativeInterface.postMessage(JSON.stringify({ type, data }))
      }

      const captcha = new TencentCaptcha('2000490482', function(res) {
        const captchaResult = []
        captchaResult.push(res.ret)

        if (res.ret === 2) {
          window._postMessage('closed')
          return
        }

        if (res.ret === 0) {
          captchaResult.push(res.ticket)
          captchaResult.push(res.randstr)
          captchaResult.push(seqid)
        }

        const content = captchaResult.join('\n')
        window._postMessage('validated', { content })
      })

      captcha.show()
    """.trimIndent())

    htmlDoc.head().appendChild(scriptTag)
    htmlWebViewRef.value!!.updateContent {
      HtmlWebViewContent(
        title = "Captcha",
        fullBody = true,
        body = htmlDoc.html()
      )
    }
  }

  fun showExitHint() {
    Globals.context.run {
      Globals.commonAlertDialog.show(
        CommonAlertDialogProps(
          onDismiss = {},
          primaryButtonText = getString(R.string.okay),
          secondaryButton = ButtonConfig(
            text = getString(R.string.no),
            onClick = {
//              routeArguments.resultDeferred.complete(false)
              Globals.navController.popBackStack()
            }
          ),
          onPrimaryButtonClick = {
            refreshCaptcha()
          },
          content = {
            Text(stringResource(id = R.string.txCaptchaExitHint))
          }
        )
      )
    }
  }

  private fun refreshCaptcha() {
    coroutineScope.launch {
      htmlWebViewRef.value!!.injectScript("captcha.show()")
    }
  }

  private suspend fun sendValidateRequest(content: String) = withContext(Dispatchers.IO) {
    val requestBuilder = Request.Builder()
      .url("https://zh.moegirl.org.cn/WafCaptcha")
      .addHeader("origin", "https://zh.moegirl.org.cn")
      .addHeader("referer", "https://zh.moegirl.org.cn/Mainpage")
      .post(content.toRequestBody("text/plain;charset=UTF-8".toMediaType()))
    commonOkHttpClient.newCall(requestBuilder.build()).execute()
  }

  val messageHandlers: HtmlWebViewMessageHandlers = mapOf(
    "closed" to {
      showExitHint()
    },

    "validated" to {
      val content = it!!.get("content").asString
      coroutineScope.launch {
        try {
          sendValidateRequest(content)
//          routeArguments.resultDeferred.complete(true)
          HomeScreenModel.needReload = true
          Globals.navController.popBackStack()
        } catch(e: Exception) {
          printRequestErr(e, "captcha验证失败")
          refreshCaptcha()
        }
      }
    }
  )

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    cacheWebViews.destroyAllInstance()
    routeArguments.removeReferencesFromArgumentPool()
  }
}