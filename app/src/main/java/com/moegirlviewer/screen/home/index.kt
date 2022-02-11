package com.moegirlviewer.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BadgedBox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.drawer.CommonDrawer
import com.moegirlviewer.screen.imageViewer.ImageViewerRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.HmoeSplashImageManager
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.navigate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@Composable
fun HomeScreen() {
  val model: HomeScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val drawerRef = remember { Ref<CustomDrawerRef>() }

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
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(themeColors.primary)
                ,
                contentAlignment = Alignment.Center
              ) {
                Spacer(modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(themeColors.error)
                )
              }
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
      StyledText(
        text = stringResource(R.string.app_name),
        color = themeColors.onPrimary
      )
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