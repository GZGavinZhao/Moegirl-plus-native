package com.moegirlviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.initialization.OnComposeCreate
import com.moegirlviewer.initialization.initializeOnCreate
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.article.ArticleScreen
import com.moegirlviewer.screen.browsingHistory.BrowsingHistoryScreen
import com.moegirlviewer.screen.browsingHistorySearch.BrowsingHistorySearchScreen
import com.moegirlviewer.screen.captcha.CaptchaRouteArguments
import com.moegirlviewer.screen.captcha.CaptchaScreen
import com.moegirlviewer.screen.category.CategoryRouteArguments
import com.moegirlviewer.screen.category.CategoryScreen
import com.moegirlviewer.screen.cloudflareCaptcha.CloudflareCaptchaScreen
import com.moegirlviewer.screen.comment.CommentRouteArguments
import com.moegirlviewer.screen.comment.CommentScreen
import com.moegirlviewer.screen.commentReply.CommentReplyRouteArguments
import com.moegirlviewer.screen.commentReply.CommentReplyScreen
import com.moegirlviewer.screen.compare.ComparePageRouteArguments
import com.moegirlviewer.screen.compare.CompareScreen
import com.moegirlviewer.screen.compare.CompareTextRouteArguments
import com.moegirlviewer.screen.contribution.ContributionRouteArguments
import com.moegirlviewer.screen.contribution.ContributionScreen
import com.moegirlviewer.screen.edit.EditRouteArguments
import com.moegirlviewer.screen.edit.EditScreen
import com.moegirlviewer.screen.home.HomeScreen
import com.moegirlviewer.screen.home.homeScreenReady
import com.moegirlviewer.screen.imageViewer.ImageViewerRouteArguments
import com.moegirlviewer.screen.imageViewer.ImageViewerScreen
import com.moegirlviewer.screen.login.LoginScreen
import com.moegirlviewer.screen.notification.NotificationScreen
import com.moegirlviewer.screen.pageRevisions.PageRevisionsRouteArguments
import com.moegirlviewer.screen.pageRevisions.PageVersionHistoryScreen
import com.moegirlviewer.screen.recentChanges.RecentChangesScreen
import com.moegirlviewer.screen.search.SearchScreen
import com.moegirlviewer.screen.searchResult.SearchResultRouteArguments
import com.moegirlviewer.screen.searchResult.SearchResultScreen
import com.moegirlviewer.screen.settings.SettingsScreen
import com.moegirlviewer.screen.splashPreview.SplashPreviewRouteArguments
import com.moegirlviewer.screen.splashPreview.SplashPreviewScreen
import com.moegirlviewer.screen.splashSetting.SplashSettingScreen
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.store.SplashImageMode
import com.moegirlviewer.theme.MoegirlPlusTheme
import com.moegirlviewer.util.*
import com.moegirlviewer.util.RouteArguments.Companion.formattedArguments
import com.moegirlviewer.util.RouteArguments.Companion.formattedRouteName
import com.moegirlviewer.view.ComposeWithSplashScreenView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@InternalCoroutinesApi
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
  val coroutineScope = CoroutineScope(Dispatchers.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Globals.context = applicationContext
    Globals.activity = this

    @Composable
    fun ContentBody() {
      MoegirlPlusTheme {
        ProvideWindowInsets {
          OnComposeCreate {
            Routes(it)
          }
        }
      }
    }

    coroutineScope.launch {
      val hasDeepLink = intent.dataString != null
      val splashImageMode = SettingsStore.common.getValue { this.splashImageMode }.first()
      val isShowSplashScreen = !hasDeepLink &&
        homeScreenReady.isActive &&
        (isMoegirl(splashImageMode != SplashImageMode.OFF, true))

      if (isShowSplashScreen) {
        withSplashScreen(
          splashImageMode = splashImageMode,
          content = { ContentBody() }
        )
      } else {
        application.initializeOnCreate()
        initializeOnCreate()
        useFreeStatusBarLayout()
        setContent { ContentBody() }
      }
    }
  }
}

@InternalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
private fun Routes(navController: NavHostController) {
  AnimatedNavHost(navController = navController, startDestination = "home") {
    animatedComposable(
      route = "home",
    ) { HomeScreen() }

    animatedComposable(
      route = ArticleRouteArguments::class.java.formattedRouteName,
      arguments = ArticleRouteArguments::class.java.formattedArguments,
    ) { ArticleScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = "login",
      animation = Animation.FADE
    ) { LoginScreen() }

    animatedComposable(
      route = CaptchaRouteArguments::class.java.formattedRouteName,
      arguments = CaptchaRouteArguments::class.java.formattedArguments,
      animation = Animation.NONE
    ) { CaptchaScreen(it.arguments!!.toRouteArguments()) }

//    animatedComposable(
//      route = "cloudflareCaptcha",
//      animation = Animation.NONE
//    ) { CloudflareCaptchaScreen() }

    animatedComposable(
      route = "search",
      animation = Animation.FADE
    ) { SearchScreen() }

    animatedComposable(
      route = SearchResultRouteArguments::class.java.formattedRouteName,
      arguments = SearchResultRouteArguments::class.java.formattedArguments,
    ) { SearchResultScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = CommentRouteArguments::class.java.formattedRouteName,
      arguments = CommentRouteArguments::class.java.formattedArguments,
    ) { CommentScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = CommentReplyRouteArguments::class.java.formattedRouteName,
      arguments = CommentReplyRouteArguments::class.java.formattedArguments,
    ) { CommentReplyScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = ImageViewerRouteArguments::class.java.formattedRouteName,
      arguments = ImageViewerRouteArguments::class.java.formattedArguments,
      animation = Animation.FADE
    ) { ImageViewerScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = CategoryRouteArguments::class.java.formattedRouteName,
      arguments = CategoryRouteArguments::class.java.formattedArguments,
      animation = Animation.ONLY_CATEGORY_PAGE,
    ) { CategoryScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = "settings",
    ) { SettingsScreen() }

    animatedComposable(
      route = "splashSetting"
    ) { SplashSettingScreen() }

    animatedComposable(
      route = SplashPreviewRouteArguments::class.java.formattedRouteName,
      arguments = SplashPreviewRouteArguments::class.java.formattedArguments,
    ) { SplashPreviewScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = "browsingHistory",
    ) { BrowsingHistoryScreen() }

    animatedComposable(
      route = "browsingHistorySearch",
    ) { BrowsingHistorySearchScreen() }

    animatedComposable(
      route = "notification",
    ) { NotificationScreen() }

    animatedComposable(
      route = EditRouteArguments::class.java.formattedRouteName,
      arguments = EditRouteArguments::class.java.formattedArguments,
    ) { EditScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = "recentChanges",
    ) { RecentChangesScreen() }

    animatedComposable(
      route = ComparePageRouteArguments::class.java.formattedRouteName,
      arguments = ComparePageRouteArguments::class.java.formattedArguments,
    ) { CompareScreen(it.arguments!!.toRouteArguments<ComparePageRouteArguments>()) }

    animatedComposable(
      route = CompareTextRouteArguments::class.java.formattedRouteName,
      arguments = CompareTextRouteArguments::class.java.formattedArguments,
    ) { CompareScreen(it.arguments!!.toRouteArguments<CompareTextRouteArguments>()) }

    animatedComposable(
      route = PageRevisionsRouteArguments::class.java.formattedRouteName,
      arguments = PageRevisionsRouteArguments::class.java.formattedArguments,
    ) { PageVersionHistoryScreen(it.arguments!!.toRouteArguments()) }

    animatedComposable(
      route = ContributionRouteArguments::class.java.formattedRouteName,
      arguments = ContributionRouteArguments::class.java.formattedArguments,
    ) { ContributionScreen(it.arguments!!.toRouteArguments()) }
  }
}

private suspend fun ComponentActivity.withSplashScreen(
  splashImageMode: SplashImageMode,
  content: @Composable () -> Unit
) = coroutineScope {
  useFullScreenLayout()

  val usingSplashImage = if (isMoegirl()) {
    when(splashImageMode) {
      SplashImageMode.NEW -> splashImageList.last()
      SplashImageMode.RANDOM -> splashImageList.random()
      SplashImageMode.CUSTOM_RANDOM -> SettingsStore.common.getValue { this.selectedSplashImages }
        .map { it.ifEmpty { listOf(splashImageList.last().key) } }
        .map { splashImageKeys -> splashImageList.filter { splashImageKeys.contains(it.key) } }
        .first()
        .random()
      else -> null
    }!!
  } else {
    val image = HmoeSplashImageManager.getRandomImage()
    SplashImage.onlyUseInSplashScreen { rememberImagePainter(image) }
  }

  val mainWithSplashView = ComposeWithSplashScreenView(
    context = this@withSplashScreen,
    splashImage = usingSplashImage
  )

  setContentView(mainWithSplashView)

  suspend fun complete() {
    mainWithSplashView.hideSplashScreen()
    useFreeStatusBarLayout()
  }

  launch {
    mainWithSplashView.appearSplashScreen()
  }

  launch {
    delay(3000)
    complete()
  }

//  launch {
//    homeScreenReady.await()
//    complete()
//  }

  application.initializeOnCreate()
  initializeOnCreate()

  mainWithSplashView.setContent(content)
}