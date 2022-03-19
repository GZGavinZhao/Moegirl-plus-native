package com.moegirlviewer.screen.randomPages.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import com.moegirlviewer.R
import com.moegirlviewer.api.page.bean.RandomPageResBean
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.background2
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.noRippleClickable
import java.lang.Float.max

@OptIn(ExperimentalCoilApi::class, ExperimentalMaterialApi::class)
@Composable
internal fun BoxScope.RandomPageItem(
  state: RandomPageItemState,
  onRemove: (() -> Unit)? = null,
) {
  val themeColors = MaterialTheme.colors
  val cardWidth = 350f
  val cardElevation = 2f
  val rotateAngle = 20

  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val anchors = remember {
    val totalWidth = cardWidth + cardElevation
    val singleSidePadding = (configuration.screenWidthDp - totalWidth) / 2
    val maxValue = totalWidth + singleSidePadding

    mapOf(
      0f to 0,
      -maxValue * density.density to 1
    )
  }
  val swipeProgress = remember(state.swipeableState.offset.value) {
    state.swipeableState.offset.value / anchors.keys.last()
  }

  LaunchedEffect(swipeProgress) {
    if (swipeProgress == 1f) {
      onRemove?.invoke()
    }
  }

  LaunchedEffect(state.otherState?.swipeableState?.offset?.value) {
    if (state.status == RandomPageItemStatus.APPEARED) {
      state.otherSwipeProgress = if (state.otherState != null) {
        state.otherState!!.swipeableState.offset.value / anchors.keys.last()
      } else 0f
    }
  }

  val riseAnimationYOffset = 10.dp * (1 - state.otherSwipeProgress)
  val swipeAnimationYOffset = 100.dp * swipeProgress
  val appearAnimationYOffset = 100.dp * (1 - state.appearAnimationValue.value)
  val animationAlpha = when(state.status) {
    RandomPageItemStatus.INITIAL -> 0.25f * state.appearAnimationValue.value
    RandomPageItemStatus.APPEARED -> 0.25f + (0.75f * state.otherSwipeProgress)
    RandomPageItemStatus.RISEN -> 1f * (1 - swipeProgress)
    else -> 1f
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .zIndex(state.zIndex)
      .alpha(animationAlpha)
      .offset(
        y = riseAnimationYOffset + swipeAnimationYOffset + appearAnimationYOffset
      ),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .width(350.dp)
        .swipeable(
          enabled = state.status == RandomPageItemStatus.RISEN && state.swipeableState.targetValue != 1,
          state = state.swipeableState,
          anchors = anchors,
          thresholds = { _, _ -> FractionalThreshold(0.8f) },
          orientation = Orientation.Horizontal,
          resistance = null,
        )
        .graphicsLayer(
          translationX = state.swipeableState.offset.value,
          rotationZ = -rotateAngle * swipeProgress,
        )
        .then(if (state.swipeableState.targetValue != 1)
          Modifier.noRippleClickable {
            if (state.status == RandomPageItemStatus.RISEN) gotoArticlePage(state.title!!)
          } else Modifier),
      elevation = max(cardElevation * (if (state.status == RandomPageItemStatus.RISEN) 1f else state.otherSwipeProgress), 1f).dp,
      shape = RoundedCornerShape(10.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        if (state.imageUrl != null) {
          var imageLoaded by rememberSaveable { mutableStateOf(false) }

          AsyncImage(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp),
            model = rememberImageRequest(data = state.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = if (imageLoaded) Alignment.TopCenter else Alignment.Center,
            onSuccess = { imageLoaded = true }
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp)
              .background(themeColors.background2),
            contentAlignment = Alignment.Center
          ) {
            StyledText(
              text = stringResource(id = R.string.noImage),
              fontSize = 20.sp,
              color = themeColors.text.tertiary
            )
          }
        }

        Column(
          modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
        ) {
          StyledText(
            text = state.title ?: "",
            fontSize = 20.sp
          )

          StyledText(
            modifier = Modifier
              .padding(top = 10.dp),
            text = if (state.introduction ?: "" != "") state.introduction!! else stringResource(id = R.string.noIntroduction),
            fontSize = 15.sp,
            color = if (state.introduction ?: "" != "") themeColors.text.primary else themeColors.text.tertiary,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
class RandomPageItemState {
  val swipeableState = SwipeableState(
    initialValue = 0,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMediumLow * 2
    )
  )
  private var viewData: RandomPageResBean.Query.MapValue? by mutableStateOf(null)
  var appearAnimationValue = Animatable(0f)
  var status by mutableStateOf(RandomPageItemStatus.INITIAL)
  var otherState: RandomPageItemState? by mutableStateOf(null)
  var otherSwipeProgress by mutableStateOf(0f)
  var zIndex by mutableStateOf(0f)

  val hasViewData get() = viewData != null
  val title get() = viewData?.title
  val introduction get() = viewData?.extract
  val imageUrl get() = viewData?.thumbnail?.source

  private var _shardResetCount: Float? = null
  private var shardResetCount: Float
    get() = _shardResetCount ?: otherState?.shardResetCount ?: 0f
    set(value) {
      if (_shardResetCount != null) {
        _shardResetCount = value
      } else {
        otherState?._shardResetCount = value
      }
    }


  fun connect(state: RandomPageItemState) {
    otherState = state
    state.otherState = this
    _shardResetCount = 1_000_000f
  }

  suspend fun appear() {
    appearAnimationValue.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        easing = LinearOutSlowInEasing,
      )
    )
    status = RandomPageItemStatus.APPEARED
  }

  suspend fun rise() {
    appearAnimationValue.animateTo(1f)
    otherSwipeProgress = 1f
    status = RandomPageItemStatus.RISEN
  }

  suspend fun reset(viewData: RandomPageResBean.Query.MapValue?) {
    otherSwipeProgress = 0f
    status = RandomPageItemStatus.INITIAL
    this.viewData = viewData
    zIndex = --shardResetCount
    swipeableState.snapTo(0)
    appearAnimationValue.snapTo(0f)
  }
}

enum class RandomPageItemStatus {
  INITIAL,
  APPEARED,
  RISEN,
  OUTED
}