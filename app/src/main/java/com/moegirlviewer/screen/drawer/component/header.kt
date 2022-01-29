package com.moegirlviewer.screen.drawer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

const val avatarSize = 75

@Composable
fun CommonDrawerHeader(
  drawerRef: CustomDrawerRef
) {
  val statusBarHeight = Globals.statusBarHeight
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(false)
  val userName by AccountStore.userName.collectAsState(null)
  val waitingNotificationTotal by AccountStore.waitingNotificationTotal.collectAsState(0)

  fun handleOnClickAvatarOrHintText() {
    scope.launch {
      drawerRef.close()
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
        .noRippleClickable { handleOnClickAvatarOrHintText() }
        .border(3.dp, themeColors.onPrimary, shape = CircleShape)
      ,
      shape = CircleShape,
    ) {
      Image(
        modifier = Modifier
          .width(avatarSize.dp)
          .height(avatarSize.dp),
        painter = (
          if (isLoggedIn)
            rememberImagePainter(Constants.avatarUrl + userName)
          else
            painterResource(R.drawable.akari)
          ),
        contentDescription = null
      )
    }
  }

  @Composable
  fun ComposedHintText() {
    StyledText(
      modifier = Modifier
        .padding(top = 20.dp)
        .noRippleClickable { handleOnClickAvatarOrHintText() }
      ,
      text = (
        if (isLoggedIn)
          stringResource(R.string.welcomeUser, userName!!)
        else
          stringResource(R.string.loginOrJoinMoegirl)
      ),
      color = themeColors.onPrimary
    )
  }

  Box() {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .sideBorder(BorderSide.TOP, statusBarHeight.dp, themeColors.primary)
        .background(themeColors.primary)
        .padding(start = 20.dp)
        .height(150.dp),
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
        RippleColorScope(color = themeColors.onPrimary) {
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
                      Text(waitingNotificationTotal.toString())
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
                tint = themeColors.onPrimary
              )
            }
          }
        }
      }
    }
  }
}