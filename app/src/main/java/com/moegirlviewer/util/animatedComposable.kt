package com.moegirlviewer.util

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import com.moegirlviewer.util.Animation.*
import kotlinx.coroutines.delay

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

private val routeMetas = mutableMapOf<String, RouteMeta>()

@ExperimentalAnimationApi
private fun getTransitions(animation: Animation): Transitions = when(animation) {
  SLIDE -> {
    val animationSpec = tween<IntOffset>(350)

    Transitions.helpful(
      enterTransition = {
        slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec)
      },
      popExitTransition = {
        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec)
      },
      assistExitTransition = {
        slideOutOfContainer(towards = AnimatedContentScope.SlideDirection.Left, animationSpec)
      },
      assistPopEnterTransition = {
        slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec)
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

enum class Animation() {
  SLIDE,
  PUSH,
  FADE,
  NONE,

  ONLY_CATEGORY_PAGE
}

class RouteMeta(
  val animation: Animation
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

private fun getRouteName(route: String): String {
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