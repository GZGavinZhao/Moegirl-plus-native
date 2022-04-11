package com.moegirlviewer.screen.home

import ArticleErrorMask
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.compable.FirstTimeSkippedLaunchedEffect
import com.moegirlviewer.compable.OneTimeLaunchedEffect
import com.moegirlviewer.component.AppHeaderIcon
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledSwipeRefreshIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.article.component.ArticleLoadingMask
import com.moegirlviewer.screen.drawer.CommonDrawer
import com.moegirlviewer.screen.drawer.CommonDrawerState
import com.moegirlviewer.screen.home.component.*
import com.moegirlviewer.screen.home.component.newPagesCard.NewPagesCard
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.SettingsStore
import com.moegirlviewer.theme.isUsePureTheme
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@Composable
fun HomeScreen() {
  val model: HomeScreenModel = hiltViewModel()
  val isUseCardsHome by SettingsStore.cardsHomePage.getValue { useCardsHome }.collectAsState(initial = null)

  if (isUseCardsHome == null) return
  LaunchedEffect(isUseCardsHome) {
    if (isUseCardsHome!!) {
      if (model.cardsDataStatus != LoadStatus.SUCCESS) model.loadCardsData()
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
    state = model.commonDrawerState,
  ) {
    model.memoryStore.Provider {
      model.cachedWebViews.Provider {
        Scaffold(
          topBar = {
            ComposedTopAppBar(
              commonDrawerState = model.commonDrawerState,
            )
          },
        ) {
          Crossfade(
            targetState = isUseCardsHome!!
          ) {
            if (it) {
              ComposedCardsHomePage()
            } else {
              ComposedArticleView()
            }
          }
        }
      }
    }
  }
}

@Composable
fun ComposedTopAppBar(
  commonDrawerState: CommonDrawerState
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
            scope.launch { commonDrawerState.open() }
          }
        )
      }
    },
    title = {
      if (isMoegirl()) {
        StyledText(
          text = stringResource(R.string.app_name),
          color = themeColors.onPrimary
        )
      } else {
        Row {
          if (isUsePureTheme()) {
            StyledText(
              text = "H",
              color = themeColors.primaryVariant,
              fontWeight = FontWeight.Black
            )
            StyledText(
              text = "Moegirl",
              color = themeColors.onPrimary
            )
          } else {
            StyledText(
              text = "HMoegirl",
              color = themeColors.onPrimary
            )
          }
        }
      }
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

  SwipeRefresh(
    state = model.swipeRefreshState,
    swipeEnabled = model.cardsDataStatus != LoadStatus.LOADING,
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
        CardPlaceholder(true)
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

@Composable
private fun ComposedArticleView() {
  val model: HomeScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  SwipeRefresh(
    state = model.swipeRefreshState,
    swipeEnabled = model.articleLoadStatus != LoadStatus.LOADING,
    onRefresh = {
      scope.launch { model.articleViewRef.value!!.reload(true) }
    },
    indicator = { state, refreshTriggerDistance ->
      StyledSwipeRefreshIndicator(
        state = state,
        refreshTriggerDistance = refreshTriggerDistance
      )
    }
  ) {
    Center {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
      ) {
        ArticleView(
          props = ArticleViewProps(
            pageName = "Mainpage",
            ref = model.articleViewRef,
            fullHeight = true,
            visibleLoadStatusIndicator = false,
            onStatusChanged = { model.articleLoadStatus = it }
          )
        )
      }
    }

    AnimatedVisibility(
      visible = model.articleLoadStatus == LoadStatus.LOADING,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      ArticleLoadingMask()
    }

    AnimatedVisibility(
      visible = model.articleLoadStatus == LoadStatus.FAIL,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      ArticleErrorMask(
        onClick = {
          scope.launch {
            model.articleViewRef.value!!.reload(true)
          }
        }
      )
    }
  }
}