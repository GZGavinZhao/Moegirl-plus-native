package com.moegirlviewer.screen.article

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.api.watchList.WatchListApi
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.articleView.ArticleCatalog
import com.moegirlviewer.component.articleView.ArticleData
import com.moegirlviewer.component.articleView.ArticleInfo
import com.moegirlviewer.component.articleView.ArticleViewRef
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.component.customDrawer.CustomDrawerState
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.room.browsingRecord.BrowsingRecord
import com.moegirlviewer.room.watchingPage.WatchingPage
import com.moegirlviewer.screen.drawer.CommonDrawerState
import com.moegirlviewer.screen.edit.EditRouteArguments
import com.moegirlviewer.screen.edit.EditType
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CommentStore
import com.moegirlviewer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ArticleScreenModel @Inject constructor() : ViewModel() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val cachedWebViews = CachedWebViews()
  val memoryStore = MemoryStore()
  val catalogDrawerState = CustomDrawerState()
  val articleViewRef = Ref<ArticleViewRef>()
  lateinit var routeArguments: ArticleRouteArguments

  var articleData by mutableStateOf<ArticleData?>(null)
  var visibleHeader by mutableStateOf(true)
  var catalogData by mutableStateOf(emptyList<ArticleCatalog>())
  var articleInfo by mutableStateOf<ArticleInfo?>(null)
  // 该页面是否在当前用户的监视列表之中，虽然这个状态也是从articleInfo拿的，但由于这个值会由用户操作动态变化，所以作为state声明
  var isWatched by mutableStateOf(false)
  var visibleFindBar by mutableStateOf(false)
  var visibleCommentButton by mutableStateOf(false)
  // 是否允许编辑，null表示权限检测中
  var editAllowed by mutableStateOf<Boolean?>(null)

  // 真实页面名
  val truePageName get() = articleData?.parse?.title ?:
    routeArguments.pageName ?:
    routeArguments.readingRecord?.pageName
  // 要显示的页面名(受萌百标题格式化模板的影响)
  val displayPageName get() = getTextFromHtml(
    (
      articleData?.parse?.displaytitle ?:
      routeArguments.displayName ?:
      routeArguments.pageName ?:
      routeArguments.readingRecord?.pageName ?:
      Globals.context.getString(R.string.app_name)
    )
      .replace("_", " ")
      .replace(categoryPageNamePrefixRegex, "${Globals.context.getString(R.string.category)}：")
  )
  val pageId get() = articleData?.parse?.pageid

  // 是否允许编辑全文，讨论页默认不允许
  val editFullDisabled get() = articleInfo?.ns != null && MediaWikiNamespace.isTalkPage(articleInfo!!.ns)
  // 是否允许显示评论按钮
  val commentButtonAllowed get() = listOf(
    MediaWikiNamespace.MAIN.code,
    MediaWikiNamespace.USER.code,
    MediaWikiNamespace.HELP.code,
    MediaWikiNamespace.PROJECT.code
  ).contains(articleInfo?.ns) && isMoegirl(true, hmoeCommentDisabledTitles.contains(truePageName).not())
  // 是否显示前往讨论页的按钮，当前为讨论页时不显示
  val visibleTalkButton get() = articleInfo?.ns != null && !MediaWikiNamespace.isTalkPage(articleInfo!!.ns)
  // 当前页面是否存在讨论页
  val isTalkPageExists get() = articleInfo?.talkid != null

  // 用于记录离开页面前是否所有媒体被禁用，如果禁用了，回来后不管stopMediaOnLeave是否开启，都要调用articleViewRef.enableAllMedia()
  // 否则页面内嵌的iframe无法正常使用，因为是通过将iframe的src清空实现的停止播放
  var isMediaDisabled = false

  val isHistoryVersion get() = routeArguments.revId != null

  fun handleOnArticleLoaded(articleData: ArticleData, articleInfo: ArticleInfo) {
    this.articleData = articleData
    this.articleInfo = articleInfo
    isWatched = articleInfo.watched != null

    coroutineScope.launch {
      delay(500)
      visibleCommentButton = true
    }

    coroutineScope.launch {
      CommentStore.loadNext(pageId!!)
    }

    coroutineScope.launch {
      val mainPageUrl = try {
        PageApi.getMainImageAndIntroduction(truePageName!!, size = 250).query.pages.values.first().thumbnail?.source
      } catch (e: MoeRequestException) { null }

      Globals.room.browsingRecord().insertItem(BrowsingRecord(
        pageName = truePageName!!,
        displayName = displayPageName,
        imgUrl = mainPageUrl
      ))
    }

    coroutineScope.launch { checkEditAllowed() }
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

  fun handleOnArticleRendered() {
    if (routeArguments.anchor != null) {
      coroutineScope.launch {
        val minusOffset = Constants.topAppBarHeight + Globals.statusBarHeight
        articleViewRef.value!!.htmlWebViewRef!!.injectScript("""
          document.getElementById('${routeArguments.anchor}').scrollIntoView()
          window.scrollTo(0, window.scrollY - $minusOffset)
        """.trimIndent())
      }
    }

    if (truePageName == "H萌娘:官方群组") {
      coroutineScope.launch {
        articleViewRef.value!!.htmlWebViewRef!!.injectScript("""
          document.getElementById('app-background').style.display = 'block'
          document.getElementById('app-background-top-padding').style.height = '${Constants.topAppBarHeight + Globals.statusBarHeight}px'
          document.body.style.maxHeight = '100%'
          document.body.style.overflowY = 'hidden'
          document.documentElement.style.overflowY = 'hidden'
        """.trimIndent())
      }
    }

    if (routeArguments.readingRecord != null) {
      coroutineScope.launch {
        articleViewRef.value!!.htmlWebViewRef!!.injectScript("""
          window.scrollTo(0, ${routeArguments.readingRecord!!.scrollY})
        """.trimIndent())
      }
    }
  }

  suspend fun handleOnGotoEditClicked() {
    val isNonautoConfirmed = checkIfNonAutoConfirmedToShowEditAlert(truePageName!!)
    if (isNonautoConfirmed) return
    Globals.navController.navigate(EditRouteArguments(
      pageName = truePageName!!,
      type = EditType.FULL
    ))
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

  suspend fun checkEditAllowed() {
    if (!AccountStore.isLoggedIn.first()) return

    if (isHistoryVersion) {
      editAllowed = false
      toast(Globals.context.getString(R.string.historyModeEditDisabledHint))
      return
    }

    try {
      val userInfo = AccountStore.loadUserInfo()
      val isUnprotectednessPage = articleInfo!!.protection.all { it.type != "edit" } || articleInfo!!.protection.isEmpty()
      val isSysop = userInfo.groups.contains("sysop")
      val isPatroller = userInfo.groups.contains("patroller")

      // 是非保护页面
      editAllowed = isUnprotectednessPage ||
        // 用户是管理员
        isSysop ||
        // 是巡查员，且当前页面的保护等级允许巡查员编辑
        (isPatroller && articleInfo!!.protection.first { it.type == "edit" }.level == "patrolleredit")
    } catch (e: MoeRequestException) {
      printRequestErr(e, "检查页面是否可编辑失败")
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

  fun jumpToAnchor(anchor: String) {
    val minusOffset = Constants.topAppBarHeight + Globals.statusBarHeight
    coroutineScope.launch {
      articleViewRef.value!!.htmlWebViewRef!!.injectScript(
        "moegirl.method.link.gotoAnchor('$anchor', -$minusOffset)"
      )
    }
  }

  fun share() {
    val siteName = Globals.context.getString(R.string.siteName)
    shareText("$siteName - ${routeArguments.pageName} ${Constants.shareUrl}$pageId")
  }

  companion object {
    // 每次进入article screen时检查这个字段，如果为true则执行reload
    var needReload = false
  }

  override fun onCleared() {
    super.onCleared()
    coroutineScope.cancel()
    cachedWebViews.destroyAllInstance()
    routeArguments.removeReferencesFromArgumentPool()
  }
}

val hmoeCommentDisabledTitles = listOf(
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