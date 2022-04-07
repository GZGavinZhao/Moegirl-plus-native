package com.moegirlviewer.screen.drawer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.drawer.CommonDrawerState
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.theme.background2
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

const val avatarSize = 75

@Composable
fun CommonDrawerHeader(
  commonDrawerState: CommonDrawerState
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(false)
  val userName by AccountStore.userName.collectAsState(null)
  val waitingNotificationTotal by AccountStore.waitingNotificationTotal.collectAsState(0)

  fun handleOnClickAvatarOrHintText() {
    scope.launch {
      launch { commonDrawerState.close() }
      if (isLoggedIn) {
        gotoUserPage(userName!!)
      } else {
        Globals.navController.navigate("login")
      }
    }
  }

  @Composable
  fun ComposedAvatar() {
    Surface(
      modifier = Modifier
        .width(avatarSize.dp)
        .height(avatarSize.dp)
        .clip(CircleShape)
        .background(themeColors.background2)
        .noRippleClickable { handleOnClickAvatarOrHintText() }
        .border(3.dp, themeColors.onSecondary, shape = CircleShape)
      ,
      shape = CircleShape,
    ) {
      AsyncImage(
        modifier = Modifier
          .width(avatarSize.dp)
          .height(avatarSize.dp),
        model = rememberImageRequest(data = if (isLoggedIn) Constants.avatarUrl + userName else R.drawable.akari) {
          error(R.drawable.akari)
        },
        contentDescription = null,
      )
    }
  }

  @Composable
  fun ComposedHintText() {
    StyledText(
      modifier = Modifier
        .padding(top = 10.dp)
        .noRippleClickable { handleOnClickAvatarOrHintText() }
      ,
      text = (
        if (isLoggedIn)
          stringResource(R.string.welcomeUser, userName!!)
        else
          stringResource(R.string.loginOrJoinMoegirl)
      ),
      color = themeColors.onSecondary
    )
  }

  Box(
    modifier = Modifier
      .height((150 + Globals.statusBarHeight).dp)
  ) {
    Image(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = remember {
            Brush.horizontalGradient(
              listOf(
                themeColors.primaryVariant,
                themeColors.primaryVariant.copy(alpha = 0.65f)
              )
            )
          }
        ),
      painter = painterResource(id = R.drawable.drawer_top_bg),
      contentDescription = null,
      contentScale = ContentScale.FillHeight
    )

    Column(
      modifier = Modifier
        .statusBarsPadding()
        .padding(start = 20.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center
    ) {
      ComposedAvatar()
      ComposedHintText()
    }

    if (isLoggedIn) {
      Box(
        modifier = Modifier
          .matchParentSize()
          .absoluteOffset((-5).dp, 20.dp),
        contentAlignment = Alignment.TopEnd
      ) {
        RippleColorScope(color = themeColors.onSecondary) {
          IconButton(
            onClick = {
              Globals.navController.navigate("notification")
            }
          ) {
            BadgedBox(
              badge = {
                if (waitingNotificationTotal != 0) {
                  Box(
                    modifier = Modifier
                      .offset((-5).dp, 2.dp)
                  ) {
                    Badge {
                      StyledText(
                        text = waitingNotificationTotal.toString(),
                        color = Color.Unspecified
                      )
                    }
                  }
                }
              }
            ) {
              Icon(
                modifier = Modifier
                  .size(24.dp),
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = themeColors.onSecondary
              )
            }
          }
        }
      }
    }
  }
}