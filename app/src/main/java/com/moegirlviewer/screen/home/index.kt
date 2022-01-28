package com.moegirlviewer.screen.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moegirlviewer.MainActivity
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.articleView.MoegirlImage
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.drawer.CommonDrawer
import com.moegirlviewer.screen.imageViewer.ImageViewerRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.noRippleClickable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@Composable
fun HomeScreen() {
  val model: HomeScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val drawerRef = Ref<CustomDrawerRef>()

  LaunchedEffect(true) {
    if (HomeScreenModel.needReload && model.articleViewRef.value!!.loadStatus == LoadStatus.LOADING) {
      model.articleViewRef.value!!.reload(true)
      HomeScreenModel.needReload = false
    }
  }

  BackHandler {
    model.triggerForTwoPressToExit()
  }

  CommonDrawer(
    ref = drawerRef
  ) {
    model.memoryStore.Provider {
      model.cachedWebViews.Provider {
        Scaffold(
          topBar = {
            ComposedTopAppBar(
              drawerRef = drawerRef,
            )
          },
        ) {
          ArticleView(
            props = ArticleViewProps(
              pageName = "Mainpage",
              ref = model.articleViewRef,
              onArticleRendered = {
                scope.launch {
                  delay(500)
                  homeScreenReady.complete(true)
                }
              }
            )
          )
        }
      }
    }
  }
}

val homeScreenReady = CompletableDeferred<Boolean>()

@Composable
fun ComposedTopAppBar(
  drawerRef: Ref<CustomDrawerRef>
) {
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val waitingNotificationTotal by AccountStore.waitingNotificationTotal.collectAsState(0)

  StyledTopAppBar(
    navigationIcon = {
      BadgedBox(
        badge = {
          if (waitingNotificationTotal != 0) {
            Box(
              modifier = Modifier
                .offset((-15).dp, 15.dp)
            ) {
              Badge(
                modifier = Modifier
                  .size(14.dp)
                  .border(
                    width = 3.dp,
                    color = themeColors.primary,
                    shape = CircleShape
                  )
                  .clip(CircleShape)
              )
            }
          }
        }
      ) {
        AppHeaderIcon(
          image = Icons.Filled.Menu,
          onClick = {
            scope.launch { drawerRef.value!!.open.invoke() }
          }
        )
      }
    },
    title = {
      Text(text = stringResource(R.string.app_name))
    },
    actions = {
      AppHeaderIcon(
        image = Icons.Filled.Search,
        onClick = {
          Globals.navController.navigate("search")
        }
      )
    },
  )
}