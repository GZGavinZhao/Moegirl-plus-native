package com.moegirlviewer.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.contribution.ContributionRouteArguments
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.navigate
import com.moegirlviewer.util.noRippleClickable

@Composable
fun UserTail(
  userName: String,
  fontSize: TextUnit = 13.sp
) {
  val themeColors = MaterialTheme.colors
  val currentTextStyle = LocalTextStyle.current
  val textStyle = remember { currentTextStyle.copy(fontSize = fontSize) }


  Row() {
    CompositionLocalProvider(
      LocalTextStyle provides textStyle
    ) {
      StyledText(
        text = " (",
        color = themeColors.text.secondary
      )
      StyledText(
        modifier = Modifier
          .noRippleClickable { gotoArticlePage("User_talk:$userName") },
        text = stringResource(id = R.string.talk),
        color = themeColors.primaryVariant
      )
      StyledText(
        text = " | ",
        color = themeColors.text.tertiary
      )
      StyledText(
        modifier = Modifier
          .noRippleClickable {
            Globals.navController.navigate(ContributionRouteArguments(userName = userName))
          },
        text = stringResource(id = R.string.contribution),
        color = themeColors.primaryVariant
      )
      StyledText(
        text = ")",
        color = themeColors.text.secondary
      )
    }
  }
}