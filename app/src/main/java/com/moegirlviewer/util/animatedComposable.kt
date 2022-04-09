package com.moegirlviewer.util

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import my.google.accompanist.navigation.animation.composable
import com.moegirlviewer.component.Center
import com.moegirlviewer.util.Animation.*
import kotlin.math.roundToInt

@ExperimentalAnimationApi
fun NavGraphBuilder.animatedComposable(
  route: String,
  arguments: List<NamedNavArgument> = emptyList(),
  deepLinks: List<NavDeepLink> = emptyList(),
  animation: Animation = SLIDE,
  content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
  routeMetas[getRouteName(route)] = RouteMeta(
    animation = animation
  )

  val transitions = getTransitions(animation)

  composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = transitions.enterTransition,
    exitTransition = transitions.exitTransition,
    popEnterTransition = transitions.popEnterTransition,
    popExitTransition = transitions.popExitTransition,
    content = content,
  )
}

val routeMetas = mutableMapOf<String, RouteMeta>()

@OptIn(InternalAnimationApi::class)
@ExperimentalAnimationApi
private fun getTransitions(animation: Animation): Transitions = when(animation) {
  SLIDE -> {
    val animationSpec = tween<IntOffset>(350)

    Transitions.helpful(
      enterTransition = {
        slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec)
      },
      popExitTransition = {
        slideOutOfContainer(
          towards = AnimatedContentScope.SlideDirection.Right,
          animationSpec = animationSpec,
        )
      },
      assistExitTransition = {
        SLIDE.animationDecorationBoxes.assistExitTransition = createMaskDecoration(tween(animationSpec.durationMillis))
        slideOutOfContainer(
          towards = AnimatedContentScope.SlideDirection.Left,
          animationSpec = animationSpec,
          targetOffset = { (0.2 * it).roundToInt() }
        )
      },
      assistPopEnterTransition = {
        SLIDE.animationDecorationBoxes.assistPopEnterTransition = createMaskDecoration(
          animationSpec = tween(animationSpec.durationMillis),
          reversed = true
        )
        slideIntoContainer(
          towards = AnimatedContentScope.SlideDirection.Right,
          animationSpec = animationSpec,
          initialOffset = { (0.2 * it).roundToInt() }
        )
      }
    )
  }

  PUSH -> {
    val durationMillis = 350
    val enterEasing = CubicBezierEasing(0.25f,0.1f,0.25f,1f)
    val exitEasing = CubicBezierEasing(1f,0f,0.54f,0.97f)
    val fadeAnimationSpec = { easing: Easing ->
      TweenSpec<Float>(
        durationMillis = durationMillis,
        easing = easing
      )
    }
    val slideAnimationSpec = { easing: Easing ->
      TweenSpec<IntOffset>(
        durationMillis = durationMillis,
        easing = easing
      )
    }

    Transitions.helpful(
      enterTransition = {
        fadeIn(fadeAnimationSpec(enterEasing)) +
          slideInVertically(
            animationSpec = slideAnimationSpec(enterEasing),
            initialOffsetY = { fullHeight -> fullHeight }
          )
      },
      popExitTransition = {
        fadeOut(fadeAnimationSpec(exitEasing)) +
          slideOutVertically(
            animationSpec = slideAnimationSpec(exitEasing),
            targetOffsetY = { fullHeight -> fullHeight }
          )
      },
      assistExitTransition = {
        fadeOut(TweenSpec(
          durationMillis = 1,
          delay = durationMillis,
        ))
      },
      assistPopEnterTransition = {
        fadeIn(TweenSpec(
          durationMillis = durationMillis,
        ))
      }
    )
  }

  FADE -> {
    val animationSpec = TweenSpec<Float>(
      durationMillis = 350,
    )

    Transitions.helpful(
      enterTransition = {
        fadeIn(animationSpec)
      },
      popExitTransition = {
        fadeOut(animationSpec)
      },
      assistExitTransition = {
        fadeOut(animationSpec)
      },
      assistPopEnterTransition = {
        fadeIn(animationSpec)
      },
    )
  }

  EXPANDED -> {
    val animationSpec = TweenSpec<Float>(
      durationMillis = 350,
    )

    Transitions.helpful(
      enterTransition = {
        fadeIn(animationSpec) +
          scaleIn(
            animationSpec = animationSpec,
            initialScale = 0.5f
          )
      },
      popExitTransition = {
        fadeOut(animationSpec) +
          scaleOut(
            animationSpec = animationSpec,
            targetScale = 0.5f
          )
      },
      assistExitTransition = {
        fadeOut(snap(animationSpec.durationMillis))
      },
      assistPopEnterTransition = {
        fadeIn(snap(0))
      },
    )
  }

  NONE -> Transitions.helpful(
    enterTransition = {
      EnterTransition.None
    },
    popExitTransition = {
      ExitTransition.None
    },
    assistExitTransition = {
      ExitTransition.None
    },
    assistPopEnterTransition = {
      EnterTransition.None
    },
  )

  ONLY_CATEGORY_PAGE -> {
    val animationSpec = tween<IntOffset>(350)

    Transitions.helpful(
      enterTransition = {
        EnterTransition.None
      },
      popExitTransition = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec)
      },
      assistExitTransition = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec)
      },
      assistPopEnterTransition = {
        slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec)
      }
    )
  }
}

enum class Animation(
  val animationDecorationBoxes: NavigationAnimationDecorationBoxes = NavigationAnimationDecorationBoxes()
) {
  SLIDE,
  PUSH,
  FADE,
  EXPANDED,
  NONE,

  ONLY_CATEGORY_PAGE
}

class RouteMeta(
  val animation: Animation
)

typealias DecorationBox = @Composable AnimatedVisibilityScope.() -> Unit

data class NavigationAnimationDecorationBoxes(
  var enterTransition: DecorationBox? = null,
  var popExitTransition: DecorationBox? = null,
  var assistExitTransition: DecorationBox? = null,
  var assistPopEnterTransition: DecorationBox? = null
)

@ExperimentalAnimationApi
private val AnimatedContentScope<NavBackStackEntry>.targetRouteMeta: RouteMeta get() {
  val routeName = getRouteName(targetState.destination.route!!)
  return routeMetas[routeName]!!
}

@ExperimentalAnimationApi
private val AnimatedContentScope<NavBackStackEntry>.initialRouteMeta: RouteMeta get() {
  val routeName = getRouteName(initialState.destination.route!!)
  return routeMetas[routeName]!!
}

fun getRouteName(route: String): String {
  return route.split("?")[0]
}

@ExperimentalAnimationApi
private class Transitions(
  val enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)?,
  val exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)?,
  val popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)?,
  val popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)?,

  val assistExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?),
  val assistPopEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)
) {
  companion object {
    // 自动配合要进入的路由及弹出路由的动画
    fun helpful(
      enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)?,
      popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)?,
      assistExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?),
      assistPopEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)
    ) = Transitions(
      enterTransition = enterTransition,
      exitTransition = {
        getTransitions(targetRouteMeta.animation).assistExitTransition(this)
      },
      popEnterTransition = {
        getTransitions(initialRouteMeta.animation).assistPopEnterTransition(this)
      },
      popExitTransition = popExitTransition,
      assistExitTransition = assistExitTransition,
      assistPopEnterTransition = assistPopEnterTransition
    )
  }
}

private fun createMaskDecoration(
  animationSpec: AnimationSpec<Float>,
  reversed: Boolean = false
): @Composable AnimatedVisibilityScope.() -> Unit = {
  val animationValue = remember { Animatable(if (reversed) 1f else 0f) }

  LaunchedEffect(true) {
    animationValue.animateTo(
      animationSpec = animationSpec,
      targetValue = if (reversed) 0f else 1f
    )
  }

  Center {
    Spacer(modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.75f * animationValue.value))
    )
  }
}