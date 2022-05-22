package com.moegirlviewer.screen.article

import androidx.compose.runtime.*
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.editingRecord.EditingRecordApi
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.watchList.WatchListApi
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.articleView.*
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.customDrawer.CustomDrawerState
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.room.watchingPage.WatchingPage
import com.moegirlviewer.screen.article.component.header.EditAllowedStatus
import com.moegirlviewer.screen.edit.EditRouteArguments
import com.moegirlviewer.screen.edit.EditType
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.store.CommonSettings
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import javax.inject.Inject

@HiltViewModel
class ArticleScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val cachedWebViews = CachedWebViews()
  val memoryStore = MemoryStore()
  val catalogDrawerState = CustomDrawerState()
  val articleViewState = ArticleViewState()
  lateinit var routeArguments: ArticleRouteArguments

  val articleData by articleViewState::articleData
  var visibleHeader by mutableStateOf(true)
  var catalogData by mutableStateOf(emptyList<ArticleCatalog>())
  val articleInfo by articleViewState::articleInfo
  // 该页面是否在当前用户的监视列表之中，虽然这个状态也是从articleInfo拿的，但由于这个值会由用户操作动态变化，所以作为state声明
  var isWatched by mutableStateOf(false)
  var visibleFindBar by mutableStateOf(false)
  var visibleCommentButton by mutableStateOf(false)
  var editAllowed by mutableStateOf(EditAllowedStatus.CHECKING)
//  var swipeRefreshState = SwipeRefreshState(false)
//  var scrollState = ScrollState(0)

  // 真实页面名
  val truePageName get() = articleData?.parse?.title ?:
    routeArguments.pageKey?.triedPageNameOrNull ?:
    routeArguments.readingRecord?.pageName
  // 要显示的页面名(受萌百标题格式化模板的影响)
  val displayPageName get() = getTextFromHtml(
    (
      articleData?.parse?.displaytitle ?:
      routeArguments.displayName ?:
      routeArguments.pageKey?.triedPageNameOrNull ?:
      routeArguments.readingRecord?.pageName ?:
      Globals.context.getString(R.string.app_name)
    )
      .replace("_", " ")
      .replace(categoryPageNamePrefixRegex, "${Globals.context.getString(R.string.category)}：")
  )
  val pageId get() = routeArguments.pageKey?.triedPageIdOrNull ?: articleData?.parse?.pageid

  // 是否允许显示评论按钮
  val commentButtonAllowed: Boolean @Composable get() {
//    val isLightRequestMode by SettingsStore.common.getValue { lightRequestMode }.collectAsState(initial = false)
    val isLightRequestMode = true
    return if (isLightRequestMode) {
      if (truePageName != null)
        truePageName!!.contains(disabledShowCommentButtonRegexForLightRequestMode).not()
          && isMoegirl(true, hmoeCommentDisabledTitles.contains(truePageName).not())
        else false
    } else {
      listOf(
        MediaWikiNamespace.MAIN.code,
        MediaWikiNamespace.USER.code,
        MediaWikiNamespace.HELP.code,
        MediaWikiNamespace.PROJECT.code
      ).contains(articleInfo?.ns) && isMoegirl(true, hmoeCommentDisabledTitles.contains(truePageName).not())
    }
  }
  // 是否显示前往讨论页的按钮，当前为讨论页时不显示
  val visibleTalkButton get() = articleInfo?.ns != null && !MediaWikiNamespace.isTalkPage(articleInfo!!.ns)
  // 当前页面是否存在讨论页
  val isTalkPageExists get() = articleInfo?.talkid != null

  // 用于记录离开页面前是否所有媒体被禁用，如果禁用了，回来后不管stopMediaOnLeave是否开启，都要调用articleViewRef.enableAllMedia()
  // 否则页面内嵌的iframe无法正常使用，因为是通过将iframe的src清空实现的停止播放
  var isMediaDisabled = false

  // 用于记录页面刚进入时是否为轻请求模式，如果是的话那么在轻请求选项关闭时，该页面需要刷新
  var isLightRequestModeWhenOpened: Boolean? = null

  suspend fun handleOnArticleLoaded() = coroutineScope {
    listOf(
      launch {
        isWatched = Globals.room.watchingPage().exists(truePageName!!)
      },
//    isWatched = articleInfo?.watched != null

      launch {
        delay(500)
        visibleCommentButton = true
      },

      launch {
        CommentStore.loadNext(pageId!!)
      },

      launch {
        val mainPageUrl = try {
          PageApi.getMainImageAndIntroduction(routeArguments.pageKey!!, size = 250).query.pages.values.first().thumbnail?.source
        } catch (e: MoeRequestException) { null }

        Globals.room.browsingRecord().insertItem(BrowsingRecord(
          pageName = truePageName!!,
          displayName = displayPageName,
          imgUrl = mainPageUrl
        ))
      },

      launch {
//      val isLightRequestMode = SettingsStore.common.getValue { lightRequestMode }.first()
        val isLightRequestMode = true
        if (isLightRequestMode) {
          // 轻请求模式直接允许，之后再点击编辑时检查权限
          editAllowed = EditAllowedStatus.ALLOWED_FULL
        } else {
          checkEditAllowed()
        }
      }
    ).forEach { it.join() }
  }

  fun handleOnArticleMissed() {
    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      content = {
        StyledText(
          text = stringResource(id = R.string.articleMissedHint)
        )
      },
      onPrimaryButtonClick = {
        Globals.navController.popBackStack()
      },
      onDismiss = {
        Globals.navController.popBackStack()
      }
    ))
  }

  suspend fun handleOnArticleRendered() {
    if (routeArguments.anchor != null) {
      val minusOffset = Constants.topAppBarHeight + Globals.statusBarHeight
      articleViewState.injectScript("""
          document.getElementById('${routeArguments.anchor}').scrollIntoView()
          window.scrollTo(0, window.scrollY - $minusOffset)
        """.trimIndent())
    }

    if (truePageName == "H萌娘:官方群组") {
      articleViewState.injectScript("""
          document.getElementById('app-background').style.display = 'block'
          document.getElementById('app-background-top-padding').style.height = '${Constants.topAppBarHeight + Globals.statusBarHeight}px'
          document.body.style.maxHeight = '100%'
          document.body.style.overflowY = 'hidden'
          document.documentElement.style.overflowY = 'hidden'

          const styleTag = document.createElement('style')
          styleTag.innerHTML = '.mw-headline::after { display: none }'
          document.head.append(styleTag)          
        """.trimIndent())
    }

    if (routeArguments.readingRecord != null) {
      articleViewState.injectScript("""
          window.scrollTo(0, ${routeArguments.readingRecord!!.scrollY})
        """.trimIndent())
    }
  }

  suspend fun handleOnGotoEditClicked() {
    // 检测有没有articleInfo，没有则加载，之后判断是否可以编辑
    if (articleInfo == null) {
      Globals.commonLoadingDialog.show()
      try {
        loadArticleInfo()
        checkEditAllowed()
        if (!editAllowed.allowed) {
          Globals.commonAlertDialog.showText(Globals.context.getString(R.string.noAllowEditThePage))
          return
        }
        if (editAllowed == EditAllowedStatus.ALLOWED_SECTION) {
          Globals.commonAlertDialog.showText(Globals.context.getString(R.string.editFullTextDisabling))
          return
        }
      } catch (e: MoeRequestException) {
        printRequestErr(e, "轻请求模式点击编辑，加载条目info时失败")
        toast(Globals.context.getString(R.string.netErr))
      } finally {
        Globals.commonLoadingDialog.hide()
      }
    }


    val isNonautoConfirmed = checkIfNonAutoConfirmedToShowEditAlert(truePageName!!)
    if (isNonautoConfirmed) return
    Globals.navController.navigate(EditRouteArguments(
      pageName = truePageName!!,
      type = EditType.FULL
    ))
  }

  suspend fun handleOnPreGotoEdit(): Boolean {
    try {
      if (articleInfo == null) {
        Globals.commonLoadingDialog.show()
        loadArticleInfo()
        checkEditAllowed()
        if (!editAllowed.allowed) {
          Globals.commonAlertDialog.showText(Globals.context.getString(R.string.noAllowEditThePage))
          return false
        }
      }
    } catch (e: MoeRequestException) {
      printRequestErr(e, "轻请求模式点击编辑章节，加载条目info时失败")
      toast(Globals.context.getString(R.string.netErr))
      return false
    } finally {
      Globals.commonLoadingDialog.hide()
    }

    return true
  }

  suspend fun handleOnAddSectionClicked() {
    val isNonautoConfirmed = checkIfNonAutoConfirmedToShowEditAlert(truePageName!!, "new")
    if (isNonautoConfirmed) return
    Globals.navController.navigate(EditRouteArguments(
      pageName = truePageName!!,
      type = EditType.SECTION,
      section = "new"
    ))
  }

  fun handleOnGotoTalk() {
    val talkPageName = if (articleInfo!!.ns == 0) {
      "${Globals.context.getString(R.string.talk)}:$truePageName"
    } else {
      truePageName!!.replaceFirst(":", if (isMoegirl()) "_talk:" else "讨论:")
    }

    if (isTalkPageExists) {
      gotoArticlePage(talkPageName)
    } else {
      Globals.commonAlertDialog.show(CommonAlertDialogProps(
        content = { StyledText(Globals.context.getString(R.string.talkPageMissedHint)) },
        secondaryButton = ButtonConfig.cancelButton(),
        onPrimaryButtonClick = {
          coroutineScope.launch {
            try {
              checkIsLoggedIn(Globals.context.getString(R.string.notLoggedInHint))
              val isNonautoConfirmed = checkIfNonAutoConfirmedToShowEditAlert(truePageName!!, "new")
              if (isNonautoConfirmed) return@launch
              Globals.navController.navigate(EditRouteArguments(
                pageName = talkPageName,
                type = EditType.SECTION,
                section = "new"
              ))
            } catch (e: NotLoggedInException) {}
          }
        }
      ))
    }
  }

  // 轻请求模式下，articleInfo不会加载，要在点击编辑按钮时再加载
  suspend fun loadArticleInfo() {
    if (articleInfo != null) return
    articleViewState.core.articleInfo = PageApi.getPageInfo(routeArguments.pageKey!!)
  }

  suspend fun getEditAllowed(): Boolean? {
    return try {
      val userInfo = AccountStore.loadUserInfo()
      val isUnprotectednessPage = articleInfo!!.protection.all { it.type != "edit" } || articleInfo!!.protection.isEmpty()
      val isSysop = userInfo.groups.contains("sysop")
      val isPatroller = userInfo.groups.contains("patroller")

      // 是非保护页面
      isUnprotectednessPage ||
        // 用户是管理员
        isSysop ||
        // 是巡查员，且当前页面的保护等级允许巡查员编辑
        (isPatroller && articleInfo!!.protection.first { it.type == "edit" }.level == "patrolleredit")
    } catch (e: MoeRequestException) {
      printRequestErr(e, "检查页面是否可编辑失败")
      null
    }
  }

  suspend fun checkEditAllowed() {
    if (!AccountStore.isLoggedIn.first() || articleInfo == null) return

    if (routeArguments.revId != null) {
      val lastEditingRecordRes = EditingRecordApi.getPageRevisions(routeArguments.pageKey!!)

      val lastEditingRecord = lastEditingRecordRes.query.pages.values.first().revisions?.first() ?: return
      if (lastEditingRecord.revid != routeArguments.revId) {
        editAllowed = EditAllowedStatus.DISABLED
        toast(Globals.context.getString(R.string.historyModeEditDisabledHint))
        return
      }
    }

    // 是否允许编辑全文，讨论页默认不允许
    val editFullDisabled = MediaWikiNamespace.isTalkPage(articleInfo!!.ns)

    editAllowed = when(getEditAllowed()) {
      true -> if (editFullDisabled) EditAllowedStatus.ALLOWED_SECTION else EditAllowedStatus.ALLOWED_FULL
      false -> EditAllowedStatus.DISABLED
      null -> EditAllowedStatus.CHECKING
    }
  }

  suspend fun togglePageIsInWatchList() {
    Globals.commonLoadingDialog.show()
    try {
      WatchListApi.setWatchStatus(truePageName!!, !isWatched)
      isWatched = !isWatched
      Globals.commonLoadingDialog.hide()

      val joinWord = Globals.context.getString(R.string.join)
      val removeWord = Globals.context.getString(R.string.remove)
      toast(Globals.context.getString(R.string.watchListOperatedHint, if (isWatched) joinWord else removeWord))
      if (isWatched) {
        Globals.room.watchingPage().insertItem(WatchingPage(truePageName!!))
      } else {
        Globals.room.watchingPage().deleteItem(WatchingPage(truePageName!!))
      }
    } catch(e: MoeRequestException) {
      printRequestErr(e, "修改监视状态失败")
      toast(e.message)
    }
  }

  suspend fun jumpToAnchor(anchor: String) {
//    val minusOffset = (Constants.topAppBarHeight + Globals.statusBarHeight)
//    val anchorPosition = articleViewRef.value!!.htmlWebViewRef!!.injectScript("moegirl.method.link.getAnchorPosition('$anchor')").toFloatOrNull()?.roundToInt()
//    if (anchorPosition != null) {
//      val scrollValue = (anchorPosition * Globals.activity.resources.displayMetrics.density - minusOffset).roundToInt()
//      scrollState.animateScrollTo(scrollValue)
//    }

    val minusOffset = Constants.topAppBarHeight + Globals.statusBarHeight
    articleViewState.injectScript(
      "moegirl.method.link.gotoAnchor('$anchor', -$minusOffset)"
    )
  }

  fun share() {
    val siteName = Globals.context.getString(R.string.siteName)
    shareText("$siteName - $truePageName ${Constants.shareUrl}$pageId")
  }

  companion object {
    // 每次进入article screen时检查这个字段，如果为true则执行reload
    var needReload = false
  }

  override fun onCleared() {
    cachedWebViews.destroyAllInstance()
    coroutineScope.cancel()
    routeArguments.removeReferencesFromArgumentPool()
    super.onCleared()
  }
}

private val disabledShowCommentButtonRegexForLightRequestMode = Regex("""
  ^([Tt]alk|讨论|討論|[Tt]emplate( talk|)|模板(讨论|討論|)|[Mm]odule( talk|)|模块(讨论|討論|)|[Cc]ategory( talk|)|分[类類](讨论|討論|)|[Uu]ser talk|用户讨论|用戶討論|萌娘百科 talk|H萌娘讨论):
""".trimIndent())

private val hmoeCommentDisabledTitles = listOf(
  "H萌娘:免责声明",
  "H萌娘:隐私政策",
  "H萌娘:创建新条目",
  "H萌娘:官方群组",
  "H萌娘:条目创建请求",
  "H萌娘:讨论版导航",
  "H萌娘:主题板块导航",
  "有关部门娘",
  "习妡萍",
  "禁评",
)

object BodyDoubleClickJs {
  private const val messageName = "bodyDoubleClicked"
  val scriptContent = """
    (() => {
      let doubleClickFlag = false
      $('body').on('click', e => {
        if (doubleClickFlag) {
          doubleClickFlag = false
          _postMessage('$messageName')
        } else {
          doubleClickFlag = true
          setTimeout(() => doubleClickFlag = false, 400)
        }
      })
    })()
  """.trimIndent()

  val messageHandler: Pair<String, (JsonObject?) -> Unit> @Composable get() {
    val model: ArticleScreenModel = hiltViewModel()
    val isFocusMode by SettingsStore.common.getValue { focusMode }.collectAsState(initial = false)

    return remember(isFocusMode) {
      messageName to {
        if (isFocusMode) {
          model.visibleHeader = !model.visibleHeader
          model.visibleCommentButton = model.visibleHeader
        }
      }
    }
  }
}