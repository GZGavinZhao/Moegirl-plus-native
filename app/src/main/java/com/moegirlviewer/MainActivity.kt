package com.moegirlviewer

import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.gestures.OverScrollConfiguration
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.node.Ref
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.decode.SvgDecoder
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.component.commonDialog.*
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.article.ArticleScreen
import com.moegirlviewer.screen.browsingHistory.BrowsingHistoryScreen
import com.moegirlviewer.screen.browsingHistorySearch.BrowsingHistorySearchScreen
import com.moegirlviewer.screen.captcha.CaptchaRouteArguments
import com.moegirlviewer.screen.captcha.CaptchaScreen
import com.moegirlviewer.screen.category.CategoryRouteArguments
import com.moegirlviewer.screen.category.CategoryScreen
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
import com.moegirlviewer.ui.theme.MoegirlPlusTheme
import com.moegirlviewer.util.Animation
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.RouteArguments.Companion.formattedArguments
import com.moegirlviewer.util.RouteArguments.Companion.formattedRouteName
import com.moegirlviewer.util.animatedComposable
import com.moegirlviewer.util.toRouteArguments
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @InternalCoroutinesApi
  @ExperimentalComposeUiApi
  @ExperimentalPagerApi
  @ExperimentalMaterialApi
  @ExperimentalFoundationApi
  @ExperimentalAnimationApi
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WebView.setWebContentsDebuggingEnabled(true)
    WebView.enableSlowWholeDocumentDraw()

    // 删除默认的顶部状态栏高度偏移
    window.decorView.systemUiVisibility =
      SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
      SYSTEM_UI_FLAG_LAYOUT_STABLE

    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    val scale = resources.displayMetrics.density
    val statusBarHeight = resources.getDimensionPixelSize(resourceId) / scale

    Globals.statusBarHeight = statusBarHeight
    Globals.activity = this

    setContent {
      MoegirlPlusTheme {
        ProvideWindowInsets {
          Routes()
        }
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
private fun Routes() {
  val themeColors = MaterialTheme.colors
  val defaultImageLoader = LocalImageLoader.current
  val navController = rememberAnimatedNavController()
  val overScrollConfig = remember {
    OverScrollConfiguration(
      glowColor = themeColors.secondary
    )
  }
  val imageLoader = remember {
    defaultImageLoader.newBuilder()
      .componentRegistry {
        add(SvgDecoder(Globals.context))
        add(GifDecoder())
      }
      .build()
  }
  val commonAlertDialogRef = remember { Ref<CommonAlertDialogRef>() }
  val commonAlertDialog2Ref = remember { Ref<CommonAlertDialogRef>() }  // 这里为了能显示最多两个全局共用Dialog所以弄成这样了，虽然有点丑
  val commonLoadingDialogRef = remember { Ref<CommonLoadingDialogRef>() }
  val commonDatePickerDialogState = remember { CommonDatePickerDialogState() }

  LaunchedEffect(true) {
    Globals.navController = navController
    Globals.imageLoader = imageLoader
    Globals.commonAlertDialog = commonAlertDialogRef.value!!
    Globals.commonAlertDialog2 = commonAlertDialog2Ref.value!!
    Globals.commonLoadingDialog = commonLoadingDialogRef.value!!
    Globals.commonDatePickerDialog = commonDatePickerDialogState
  }

  CompositionLocalProvider(
    LocalImageLoader provides imageLoader,
    LocalOverScrollConfiguration provides overScrollConfig,
  ) {
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

    CommonDatePickerDialog(state = commonDatePickerDialogState)
    CommonAlertDialog(ref = commonAlertDialogRef)
    CommonAlertDialog(ref = commonAlertDialog2Ref)
    CommonLoadingDialog(ref = commonLoadingDialogRef)
  }
}