package com.moegirlviewer.component.articleView.util

import com.moegirlviewer.component.articleView.ArticleCatalog
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.api.account.AccountApi
import com.moegirlviewer.component.articleView.ArticleViewStateCore
import com.moegirlviewer.component.articleView.MoegirlImage
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.commonDialog.CommonLoadingDialogProps
import com.moegirlviewer.component.htmlWebView.HtmlWebViewMessageHandlers
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.edit.EditRouteArguments
import com.moegirlviewer.screen.edit.EditType
import com.moegirlviewer.screen.imageViewer.ImageViewerRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.util.*

fun ArticleViewStateCore.createDefaultMessageHandlers(): HtmlWebViewMessageHandlers {
  return mapOf(
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

        if (linkDisabled) return@to
        // 明明没用到协程，有时候却报“方法必须在主线程调用”，暂时没搞明白，先套个withContext
        coroutineScope.launch {
          withContext(Dispatchers.Main) {
            Globals.navController.navigate(
              ArticleRouteArguments(
                pageKey = PageNameKey(pageName),
                displayName = displayName,
                anchor = anchor
              )
            )
          }
        }
      }

      if (linkType == "img") {
        val imagesJsonArr = linkData.getAsJsonArray("images")
        val clickedIndex = linkData.get("clickedIndex").asInt

        val images = Gson().fromJson(imagesJsonArr, Array<MoegirlImage>::class.java)
        coroutineScope.launch {
          Globals.commonLoadingDialog.show(
            CommonLoadingDialogProps(
              title = Globals.context.getString(R.string.gettingImageUrl),
            )
          )
          try {
            if (this@createDefaultMessageHandlers.imgOriginalUrls.isEmpty()) {
              loadImgOriginalUrls()
            }

            images.forEach { it.fileUrl = imgOriginalUrls[it.fileName]!! }
            Globals.navController.navigate(
              ImageViewerRouteArguments(
                images = images.toList(),
                initialIndex = clickedIndex
              )
            )
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
        coroutineScope.launch {
          htmlWebViewRef.value!!.injectScript(
            "moegirl.method.link.gotoAnchor('$id', -${contentTopPadding.value})"
          )
        }
      }

      if (linkType == "notExist") {
        Globals.commonAlertDialog.showText(Globals.context.getString(R.string.pageNameMissing))
      }

      if (linkType == "edit") {
        coroutineScope.launch {
          if (onPreGotoEdit?.invoke() == false) return@launch

          val section = linkData.get("section").asString
          val pageName = linkData.get("pageName").asString
          val preload = if (linkData.has("preload")) linkData.get("preload").asString else null

          if (linkDisabled) return@launch

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

          if (!editAllowed) {
            Globals.commonAlertDialog.showText(Globals.context.getString(R.string.insufficientPermissions))
            return@launch
          }

          val isNonAutoConfirmed = checkIfNonAutoConfirmedToShowEditAlert(pageName, section)
          if (!isNonAutoConfirmed) {
            Globals.navController.navigate(
              EditRouteArguments(
                // Hmoe通过编辑链接获取的标题有问题，只能使用articleData的了，但这样就导致只能编辑当前看的页面
                pageName = isMoegirl(pageName, articleData!!.parse.title),
                type = EditType.SECTION,
                section = section,
                preload = preload
              )
            )
          }
        }
      }

      if (linkType == "watch") {

      }

      if (linkType == "external") {
        val url = linkData.get("url").asString
        if (!linkDisabled) openHttpUrl(url)
      }

      if (linkType == "unparsed") {}
    },

    "catalogData" to {
      val catalog = Gson().fromJson(it!!.getAsJsonArray("value"), Array<ArticleCatalog>::class.java).toList()
      emitCatalogData?.invoke(catalog)
    },

    "loaded" to {
      if (articleHtml != "") {
        coroutineScope.launch {
          delay(renderDelay)
          status = LoadStatus.SUCCESS
          onArticleRendered?.invoke()
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

      coroutineScope.launch {
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
        coroutineScope.launch {
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
}