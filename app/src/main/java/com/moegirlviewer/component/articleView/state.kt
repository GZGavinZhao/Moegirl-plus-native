package com.moegirlviewer.component.articleView

import android.os.Parcelable
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.google.gson.Gson
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.account.AccountApi
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.compable.remember.rememberFromMemory
import com.moegirlviewer.component.articleView.util.collectCategoryDataFromHtml
import com.moegirlviewer.component.articleView.util.createMoegirlRendererConfig
import com.moegirlviewer.component.articleView.util.showNoteDialog
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.commonDialog.CommonLoadingDialogProps
import com.moegirlviewer.component.htmlWebView.HtmlWebViewContent
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.htmlWebView.HtmlWebViewRef
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.*
import com.moegirlviewer.room.pageContentCache.PageContentCache
import com.moegirlviewer.room.pageNameRedirect.PageNameRedirect
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.category.CategoryRouteArguments
import com.moegirlviewer.screen.edit.EditRouteArguments
import com.moegirlviewer.screen.edit.EditType
import com.moegirlviewer.screen.imageViewer.ImageViewerRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CommonSettings
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.parcelize.Parcelize
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.util.*

val defaultInjectedFiles = listOf("main.css", "main.js")

class ArticleViewState(
  var context: ComposableContext,
  var props: ArticleViewProps,
) {
  var articleData by mutableStateOf<ArticleData?>(null)
  var status by mutableStateOf(LoadStatus.INITIAL)
  var contentHeight by mutableStateOf(0f)

  var imgOriginalUrls = mapOf<String, String>()
  var isInitialized = false

  val htmlWebViewRef = Ref<HtmlWebViewRef>()

  val articleHtml: String
    get() = when {
      props.html != null -> props.html!!
      else -> (articleData?.parse?.text?._asterisk) ?: ""
    }

  suspend fun updateHtmlView(force: Boolean = false) {
    if (isInitialized && !force) { return }

    val moegirlRendererConfig = createMoegirlRendererConfig(
      pageName = props.pageName,
      language = if(isTraditionalChineseEnv()) "zh-hant" else "zh-hans",
      site = Constants.source.code,
      enabledCategories = props.addCategories,
      heimu = SettingsStore.common.getValue { this.heimu }.first(),
      enabledHeightObserver = props.fullHeight,
      addCopyright = props.addCopyright,
      nightMode = context.isDarkTheme,
      categories = articleData?.parse?.categories
        ?.filter { it.hidden == null }
        ?.map { it._asterisk } ?: emptyList()
    )

    val useSpecialCharSupportedFont = SettingsStore.common.getValue { this.useSpecialCharSupportedFontInArticle }.first()

    val styles = """
      @font-face {
        font-family: "NospzGothicMoe";
        src: url("font/nospz_gothic_moe.ttf");
      }     

      body {
        ${if (useSpecialCharSupportedFont) """
          font-family: sans-serif, "NospzGothicMoe";
        """ else ""}
        padding-top: ${props.contentTopPadding.value}px;
        word-break: ${if (props.inDialogMode) "break-all" else "initial"};
        ${if (props.inDialogMode) """
          margin: 0 !important;
          padding: 0;
          max-width: 100% !important;
        """.trimIndent() else ""}
        ${if (props.inDialogMode && context.isDarkTheme) 
          "background-color: ${context.themeColors.surface.toCssRgbaString()} !important" 
        else ""}
      }
      
      ${if (props.inDialogMode) """
        p {
          margin: 0;
        }
      """.trimIndent() else ""}
      
      :root {
        --color-primary: ${context.themeColors.primary.toCssRgbaString()};
        --color-dark: ${context.themeColors.primary.darken(0.3F).toCssRgbaString()};
        --color-light: ${context.themeColors.primary.lighten(0.3F).toCssRgbaString()};
      }
    """.trimIndent()

    val cookieManager = CookieManager.getInstance()
    val cookies = cookieJar.loadForRequest(Constants.domain.toHttpUrl())

    coroutineScope {
      for (cookie in cookies) {
        async {
          val completableDeferred = CompletableDeferred<Unit>()
          cookieManager.setCookie(Constants.domain, cookie.toString()) {
            completableDeferred.complete(Unit)
          }
        }
      }
    }

    val messageForLoaded = """
      setTimeout(() => _postMessage('loaded'))
    """.trimIndent()

    val injectedStyles = listOf(styles) + (props.injectedStyles ?: emptyList())
    val injectedScripts = listOf(
      moegirlRendererConfig,
      *(props.injectedScripts ?: emptyList()).toTypedArray(),
      messageForLoaded
    )

    htmlWebViewRef.value!!.updateContent {
      HtmlWebViewContent(
        body = articleHtml,
        title = props.pageName,
        injectedStyles = injectedStyles,
        injectedScripts = injectedScripts,
        injectedFiles = defaultInjectedFiles
      )
    }

    isInitialized = true
  }

  suspend fun loadImgOriginalUrls() {
    try {
      imgOriginalUrls = PageApi.getImagesUrl(articleData!!.parse.images)
    } catch (e: MoeRequestException) {
      printRequestErr(e, "获取条目内全部图片原始链接失败")
    }
  }

  suspend fun loadArticleContent(
    pageName: String? = props.pageName,
    pageId: Int? = props.pageId,
    revId: Int? = props.revId,
    forceLoad: Boolean = false
  ) = coroutineScope {
    suspend fun consumeArticleData(articleData: ArticleData, articleInfo: ArticleInfo) {
      this@ArticleViewState.articleData = articleData
      updateHtmlView(true)
      loadImgOriginalUrls()
      props.onArticleLoaded?.invoke(articleData, articleInfo)
    }

    status = LoadStatus.LOADING

    launch {
      try {
        val truePageName = PageApi.getTruePageName(pageName, pageId)
        if (truePageName == null) {
          props.onArticleMissed?.invoke()
          return@launch
        }

        val pageInfo = PageApi.getPageInfo(truePageName)
        val isCategoryPage = MediaWikiNamespace.CATEGORY.code == pageInfo.ns

        val articleData = PageApi.getPageContent(truePageName, revId)

        if (isCategoryPage) {
          val collectedCategoryData = collectCategoryDataFromHtml(articleData.parse.text._asterisk)
          Globals.navController.replace(CategoryRouteArguments(
            categoryName = truePageName.replaceFirst(categoryPageNamePrefixRegex, ""),
            parentCategories = collectedCategoryData.parentCategories,
            categoryExplainPageName = collectedCategoryData.categoryExplainPageName
          ))

          return@launch
        }

        consumeArticleData(articleData, pageInfo)

        Globals.room.pageContentCache().insertItem(PageContentCache(
          pageName = truePageName,
          content = articleData,
          pageInfo = pageInfo
        ))
        if (pageName != null && pageName != truePageName) {
          Globals.room.pageNameRedirect().insertItem(PageNameRedirect(
            redirectName = pageName,
            pageName = truePageName
          ))
        }
      } catch (e: MoeRequestException) {
        printRequestErr(e, "加载文章失败")
        val getCrossWikiTitleFromErrorMessageRegex = Regex(""""萌百:(.+?)"""")
        when {
          e is MoeRequestWikiException && e.code == "missingtitle" -> props.onArticleMissed?.invoke()
          e is MoeRequestWikiException && e.code == "invalidtitle" && e.message.contains(getCrossWikiTitleFromErrorMessageRegex) -> {
            val crossWikiTitle = getCrossWikiTitleFromErrorMessageRegex.find(e.message)?.groupValues?.get(1)
            if (crossWikiTitle != null) {
              openHttpUrl("https://zh.moegirl.org.cn/$crossWikiTitle")
              Globals.navController.popBackStack()
            } else {
              props.onArticleError?.invoke()
              status = LoadStatus.FAIL
            }
          }
        }
        if (e is MoeRequestWikiException && e.code == "missingtitle") {
          props.onArticleMissed?.invoke()
        } else {
          props.onArticleError?.invoke()
          status = LoadStatus.FAIL
        }
      }
    }
  }

  suspend fun checkUserConfig() {
    val heimu = SettingsStore.common.getValue { this.heimu }.first()
    val useSpecialCharSupportedFont = SettingsStore.common.getValue { this.useSpecialCharSupportedFontInArticle }.first()
    // stopMediaOnLeave没法在这里处理，articleView不知道什么时候离开页面，这部分逻辑写在了articleScreen
//    val stopMediaOnLeave = SettingsStore.stopAudioOnLeave.first()

    if (heimu != context.userConfig.heimu) {
      htmlWebViewRef.value!!.injectScript("moegirl.config.heimu.\$enabled = $heimu")
      context.userConfig.heimu = heimu
    }

    if (useSpecialCharSupportedFont != context.userConfig.useSpecialCharSupportedFont) {
      if (useSpecialCharSupportedFont)
        enableSpecialCharSupportedFont() else
        disableSpecialCharSupportedFont()
      context.userConfig.useSpecialCharSupportedFont = useSpecialCharSupportedFont
    }
  }

  suspend fun disableAllMedia() {
    htmlWebViewRef.value!!.injectScript("""
      $disableAllIframeJsStr;
      $pauseAllAudioJsStr
    """.trimIndent())
  }

  suspend fun enableAllMedia() {
    htmlWebViewRef.value!!.injectScript(enableAllIframeJsStr)
  }

  suspend fun enableSpecialCharSupportedFont() {
    htmlWebViewRef.value!!.injectScript("""
      document.body.style.fontFamily = 'sans-serif, "NospzGothicMoe"'
    """.trimIndent())
  }

  suspend fun disableSpecialCharSupportedFont() {
    htmlWebViewRef.value!!.injectScript("""
      document.body.style.fontFamily = 'initial'
    """.trimIndent())
  }

  // h萌娘在被waf拦截后所有资源需要带cookie，由于不同源cookie没法带过去，这里需要代理加载资源
  fun shouldInterceptRequest(webView: WebView, request: WebResourceRequest): WebResourceResponse? {
    if (request.url.toString().contains(Constants.domain.replace("https://", "")).not()) return null

    val urlStr = request.url.toString()
      .replace(Regex("""^file://"""), "https://")

    val okhttpRequest = Request.Builder()
      .url(urlStr)
      .build()

    val res = try {
      moeOkHttpClient.newCall(okhttpRequest).execute()
    } catch (e: Exception) {
      printRequestErr(e, "宿主代理webView加载资源失败")
      return null
    }

    val byteStream = res.body?.byteStream()

    return if (byteStream != null) {
      WebResourceResponse(
        res.headers["content-type"],
        "utf-8",
        byteStream
      )
    } else {
      null
    }
  }

  val defaultMessageHandlers: HtmlWebViewMessageHandlers = mapOf(
    "link" to { data ->
      val linkType = data!!.get("type").asString
      val linkData = data.get("data").asJsonObject

      if (linkType == "article") {
        val pageName = linkData.get("pageName").asString
        val anchor = linkData.get("anchor")?.asString
        val displayName = linkData.get("displayName")?.asString

        if (pageName.contains(Regex("""^(Special|特殊):"""))) {
          Globals.commonAlertDialog.showText(Globals.context.getString(R.string.specialLinkUnsupported))
          return@to
        }

        if (props.linkDisabled) return@to
        // 明明没用到协程，有时候却报“方法必须在主线程调用”，暂时没搞明白，先套个withContext
        scope.launch {
          withContext(Dispatchers.Main) {
            Globals.navController.navigate(ArticleRouteArguments(
              pageName = pageName,
              displayName = displayName,
              anchor = anchor
            ))
          }
        }
      }

      if (linkType == "img") {
        val imagesJsonArr = linkData.getAsJsonArray("images")
        val clickedIndex = linkData.get("clickedIndex").asInt

        val images = Gson().fromJson(imagesJsonArr, Array<MoegirlImage>::class.java)
        scope.launch {
          Globals.commonLoadingDialog.show(CommonLoadingDialogProps(
            title = Globals.context.getString(R.string.gettingImageUrl),
          ))
          try {
            if (this@ArticleViewState.imgOriginalUrls.isEmpty()) {
              loadImgOriginalUrls()
            }

            images.forEach { it.fileUrl = imgOriginalUrls[it.fileName]!! }
            Globals.navController.navigate(ImageViewerRouteArguments(
              images = images.toList(),
              initialIndex = clickedIndex
            ))
          } catch (e: MoeRequestException) {
            printRequestErr(e, "用户触发获取图片原始链接失败")
            toast(Globals.context.getString(R.string.getImageUrlFail))
          } finally {
            Globals.commonLoadingDialog.hide()
          }
        }
      }

      if (linkType == "note") {
        val html = linkData.get("html").asString
        showNoteDialog(html)
      }

      if (linkType == "anchor") {
        val id = linkData.get("id").asString
        scope.launch {
          htmlWebViewRef.value!!.injectScript(
            "moegirl.method.link.gotoAnchor('$id', -${props.contentTopPadding.value})"
          )
        }
      }

      if (linkType == "notExist") {
        Globals.commonAlertDialog.showText(Globals.context.getString(R.string.pageNameMissing))
      }

      if (linkType == "edit") {
        val section = linkData.get("section").asString
        val pageName = linkData.get("pageName").asString
        val preload = if (linkData.has("preload")) linkData.get("preload").asString else null

        if (props.linkDisabled) return@to

        scope.launch {
          val isLoggedIn = AccountStore.isLoggedIn.first()
          if (!isLoggedIn) {
            Globals.commonAlertDialog.show(CommonAlertDialogProps(
              secondaryButton = ButtonConfig.cancelButton(),
              onPrimaryButtonClick = {
                 Globals.navController.navigate("login")
              },
              content = {
                StyledText(Globals.context.getString(R.string.notLoggedInHint))
              }
            ))
            return@launch
          }

          if (!props.editAllowed) {
            Globals.commonAlertDialog.showText(Globals.context.getString(R.string.insufficientPermissions))
            return@launch
          }

          val isNonAutoConfirmed = checkIfNonAutoConfirmedToShowEditAlert(pageName, section)
          if (!isNonAutoConfirmed) {
            Globals.navController.navigate(EditRouteArguments(
              // Hmoe通过编辑链接获取的标题有问题，只能使用articleData的了，但这样就导致只能编辑当前看的页面
              pageName = isMoegirl(pageName, articleData!!.parse.title),
              type = EditType.SECTION,
              section = section,
              preload = preload
            ))
          }
        }
      }

      if (linkType == "watch") {

      }

      if (linkType == "external") {
        val url = linkData.get("url").asString
        if (!props.linkDisabled) openHttpUrl(url)
      }

      if (linkType == "unparsed") {}
    },

    "catalogData" to {
      val catalog = Gson().fromJson(it!!.getAsJsonArray("value"), Array<ArticleCatalog>::class.java).toList()
      props.emitCatalogData?.invoke(catalog)
    },

    "loaded" to {
      if (articleHtml != "") {
        scope.launch {
          delay(props.renderDelay)
          status = LoadStatus.SUCCESS
          props.onArticleRendered?.invoke()
        }
      }
    },

    "biliPlayer" to {
      val type = it!!.get("type").asString
      val videoId = it.get("videoId").asString
      val page = it.get("page").asInt

      openHttpUrl("https://www.bilibili.com/video/$type$videoId?p=$page")
    },

    "biliPlayerLongPress" to {

    },

    "request" to {
      val url = it!!.get("url").asString
      val method = it.get("method").asString
      val requestParams = it.get("data").asJsonObject
      val callbackId = it.get("callbackId").asInt

      scope.launch {
        try {
          val httpUrl = url.toHttpUrl().newBuilder()
          val formBody = FormBody.Builder()

          requestParams.entrySet().forEach {
            if (method == "get") {
              httpUrl.addQueryParameter(it.key, it.value.asString)
            } else {
              formBody.add(it.key, it.value.asString)
            }
          }

          val request = Request.Builder()
            .url(httpUrl.build())
            .method(method.uppercase(Locale.ROOT), if (method == "get") null else formBody.build())
            .build()

          withContext(Dispatchers.IO) {
            val res = commonOkHttpClient.newCall(request).execute()
            if (res.isSuccessful) {
              htmlWebViewRef.value!!.injectScript(
                "moegirl.config.request.callbacks['$callbackId'].resolve(${res.body!!.string()})"
              )
            } else {
              throw Exception(res.body?.string())
            }
          }
        } catch (e: Exception) {
          printRequestErr(e, "webView代理请求失败")
          val errorJson = "{ info: \"${e.message}\" }"
          htmlWebViewRef.value!!.injectScript(
            "moegirl.config.request.callbacks['$callbackId'].reject($errorJson)\""
          )
        }
      }
    },

    "vibrate" to {

    },

    "pageHeightChange" to {
      val height = it!!.get("value").asFloat
      contentHeight = height
    },

    "poll" to {
      val pollId = it!!.get("pollId").asString
      val answer = it.get("answer").asInt.toString()
      val token = it.get("token").asString

      try {
        scope.launch {
          val willUpdateContent = AccountApi.poll(pollId, answer, token).toUnicodeForInjectScriptInWebView()
          htmlWebViewRef.value!!.injectScript(
            "moegirl.method.poll.updatePollContent('$pollId', '$willUpdateContent')"
          )
        }
      } catch (e: MoeRequestException) {
        printRequestErr(e, "投票失败")
      }
    }
  )

  companion object {
    @Composable
    fun remember(props: ArticleViewProps): ArticleViewState {
      val themeColors = MaterialTheme.colors
      val density = LocalDensity.current
      val userConfig = rememberSaveable { ArticleViewUserConfig() }

      val state = rememberFromMemory("state") {
        ArticleViewState(
          props = props,
          context = ComposableContext(
            isDarkTheme = !themeColors.isLight,
            themeColors = themeColors,
            density = density,
            userConfig = userConfig
          )
        )
      }

      SideEffect {
        state.props = props
        state.context = ComposableContext(
          isDarkTheme = !themeColors.isLight,
          themeColors = themeColors,
          density = density,
          userConfig = userConfig
        )
      }

      return state
    }

    class ComposableContext(
      val isDarkTheme: Boolean,
      val themeColors: Colors,
      val density: Density,
      val userConfig: ArticleViewUserConfig
    )
  }
}

@ProguardIgnore
class MoegirlImage(
  val fileName: String,
  val title: String,
  var fileUrl: String = ""
)

@Parcelize
class ArticleViewUserConfig(
  var heimu: Boolean = CommonSettings().heimu,
  var useSpecialCharSupportedFont: Boolean = CommonSettings().useSpecialCharSupportedFontInArticle
) : Parcelable

const val disableAllIframeJsStr = """
  (() => {
    const iframeList = document.querySelectorAll('iframe')
    iframeList.forEach(item => {
      // 通过清空src的方式停止播放
      const src = item.src
      item.src = ''
      item.dataset.src = src
    })
  })()
"""

const val enableAllIframeJsStr = """
  (() => {
    const iframeList = document.querySelectorAll('iframe')
    iframeList.forEach(item => {
      item.src = item.dataset.src
    })
  })()
"""

const val pauseAllAudioJsStr = """
  (() => {
    const audioList = document.querySelectorAll('audio')
    audioList.forEach(item => item.pause())
  })()
"""