package com.moegirlviewer.screen.home

import ArticleErrorMask
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.moegirlviewer.R
import com.moegirlviewer.component.TopAppBarIcon
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.articleView.ArticleView
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
      if (HomeScreenModel.needReload || model.articleViewState.status == LoadStatus.LOADING) {
        model.articleViewState.reload(true)
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
        TopAppBarIcon(
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
      TopAppBarIcon(
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

      TopAppBarIcon(
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
  val themeColors = MaterialTheme.colors
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
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (model.cardsDataStatus == LoadStatus.LOADING) {
        CardPlaceholder(true)
        CardPlaceholder()
        CardPlaceholder()
      } else {
        if (isMoegirl()) {
          ArticleViewCard(
            state = model.moegirlHomeTopArticleCardState,
            pageKey = PageNameKey("User:東東君/app/homeTopCard"),
          )
        } else {
          CarouseCard(model.carouseCard)
        }
        NewPagesCard(model.newPagesCardState)
        RandomPageCard(model.randomPageCardState)
        RecommendationCard(model.recommendationCardState)
        if (!isMoegirl()) {
          Row(
            modifier = Modifier
              .padding(top = 10.dp, bottom = 20.dp)
              .fillMaxWidth()
              .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            RippleColorScope(color = themeColors.primaryVariant) {
              StyledText(
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { gotoArticlePage("活动:文段解读大赛") }
                  .weight(1f)
                  .border(2.dp, themeColors.primaryVariant, CircleShape)
                  .padding(vertical = 7.5.dp),
                text = "活动",
                color = themeColors.primaryVariant,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
            Box(
              modifier = Modifier
                .padding(horizontal = 15.dp)
                .weight(2f)
                .clip(RoundedCornerShape(50))
                .clickable { gotoArticlePage("H萌娘:主题板块导航") }
                .background(themeColors.primaryVariant)
                .padding(vertical = 7.5.dp, horizontal = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              StyledText(
                text = stringResource(id = R.string.moreTopicBlock),
                color = themeColors.onSecondary,
                fontSize = 16.sp
              )
            }
            RippleColorScope(color = themeColors.primaryVariant) {
              StyledText(
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { gotoArticlePage("Help:沙盒") }
                  .weight(1f)
                  .border(2.dp, themeColors.primaryVariant, CircleShape)
                  .padding(vertical = 7.5.dp, horizontal = 10.dp),
                text = "沙盒",
                color = themeColors.primaryVariant,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ComposedArticleView() {
  val model: HomeScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  ArticleView(
    state = model.articleViewState,
    pageKey = PageNameKey("Mainpage"),
    fullHeight = true,
    visibleLoadStatusIndicator = false,
  )

  AnimatedVisibility(
    visible = model.articleViewState.status == LoadStatus.LOADING,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    ArticleLoadingMask()
  }

  AnimatedVisibility(
    visible = model.articleViewState.status == LoadStatus.FAIL,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    ArticleErrorMask(
      onClick = {
        scope.launch {
          model.articleViewState.reload(true)
        }
      }
    )
  }
}