package com.moegirlviewer.screen.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BadgedBox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.drawer.CommonDrawer
import com.moegirlviewer.screen.home.component.*
import com.moegirlviewer.screen.home.component.newPagesCard.NewPagesCard
import com.moegirlviewer.screen.imageViewer.ImageViewerRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.CardsHomePageSettings
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.background2
import com.moegirlviewer.util.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@Composable
fun HomeScreen() {
  val model: HomeScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()
  val drawerRef = remember { Ref<CustomDrawerRef>() }
  val isUseCardsHome by SettingsStore.cardsHomePage.getValue { useCardsHome }.collectAsState(initial = true)

  LaunchedEffect(isUseCardsHome) {
    if (isUseCardsHome) {
      if (model.cardsDataStatus == LoadStatus.INITIAL) model.loadCardsData()
    } else {
      if (HomeScreenModel.needReload || model.articleViewRef.value?.loadStatus == LoadStatus.LOADING) {
        model.articleViewRef.value?.reload?.invoke(true)
        HomeScreenModel.needReload = false
      }
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
          Crossfade(
            targetState = isUseCardsHome
          ) {
            if (it) {
              ComposedCardsHomePage()
            } else {
              ArticleView(
                props = ArticleViewProps(
                  pageName = "Mainpage",
                  ref = model.articleViewRef,
                )
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ComposedTopAppBar(
  drawerRef: Ref<CustomDrawerRef>
) {
  val scope = rememberCoroutineScope()
  val themeColors = MaterialTheme.colors
  val waitingNotificationTotal by AccountStore.waitingNotificationTotal.collectAsState(0)
  val isUseCardsHome by SettingsStore.cardsHomePage.getValue { useCardsHome }.collectAsState(initial = true)

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
        image = if (isUseCardsHome) Icons.Filled.Layers else Icons.Filled.TextSnippet,
        onClick = {
          scope.launch {
            SettingsStore.cardsHomePage.setValue { useCardsHome = !useCardsHome }
            toast(Globals.context.getString(
              R.string.toggleToXXMode,
              Globals.context.getString(if (isUseCardsHome) R.string.card else R.string.webPage)
            ))
          }
        }
      )

      AppHeaderIcon(
        image = Icons.Filled.Search,
        onClick = {
          Globals.navController.navigate("search")
        }
      )
    },
  )
}

@Composable
private fun ComposedCardsHomePage() {
  val model: HomeScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  LaunchedEffect(model.cardsDataStatus) {
    model.swipeRefreshState.isRefreshing = model.cardsDataStatus == LoadStatus.LOADING
  }

  SwipeRefresh(
    state = model.swipeRefreshState,
    onRefresh = {
      scope.launch {
        model.loadCardsData()
      }
    },
    indicator = { state, trigger ->
      StyledSwipeRefreshIndicator(state, trigger)
    }
  ) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
    ) {
      if (model.cardsDataStatus == LoadStatus.LOADING) {
        CardPlaceholder()
        CardPlaceholder()
        CardPlaceholder()
      } else {
        if (isMoegirl()) {
          TopCard(model.topCardState)
        } else {
          CarouseCard(model.carouseCard)
        }
        NewPagesCard(model.newPagesCardState)
        RandomPageCard(model.randomPageCardState)
        RecommendationCard(model.recommendationCardState)
      }
    }
  }
}