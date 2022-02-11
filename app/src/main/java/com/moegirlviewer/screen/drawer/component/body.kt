package com.moegirlviewer.screen.drawer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.ImagePainter
import com.moegirlviewer.R
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.customDrawer.CustomDrawerRef
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.contribution.ContributionRouteArguments
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.gotoArticlePage
import com.moegirlviewer.util.isMoegirl
import com.moegirlviewer.util.navigate

@Composable
fun CommonDrawerBody(
  modifier: Modifier = Modifier,
  drawerRef: CustomDrawerRef
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)
  val userNameOfCurrentAccount by AccountStore.userName.collectAsState(initial = "")

  fun withDrawerClosed(exec: () -> Unit) {
    drawerRef.close()
    exec()
  }

  RippleColorScope(themeColors.secondary) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .then(modifier)
    ) {
      Item(
        icon = Icons.Filled.Forum,
        text = stringResource(id = R.string.talkPage),
        onClick = {
          withDrawerClosed {
            Globals.navController.navigate(ArticleRouteArguments(
              pageName = isMoegirl("萌娘百科 talk:讨论版", "H萌娘讨论:讨论版")
            ))
          }
        }
      )

      Item(
        icon = Icons.Filled.FormatIndentDecrease,
        text = stringResource(id = R.string.recentChanges),
        onClick = {
          withDrawerClosed {
            Globals.navController.navigate("recentChanges")
          }
        }
      )

      Item(
        icon = Icons.Filled.History,
        text = stringResource(id = R.string.browseHistory),
        onClick = {
          withDrawerClosed {
            Globals.navController.navigate("browsingHistory")
          }
        }
      )

      if (isLoggedIn) {
        Item(
          icon = ImageVector.vectorResource(id = R.drawable.book_open_page_variant),
          text = stringResource(id = R.string.myContribution),
          onClick = {
            withDrawerClosed {
              Globals.navController.navigate(ContributionRouteArguments(
                userName = userNameOfCurrentAccount!!
              ))
            }
          }
        )
      }

      if (!isMoegirl()) {
        Item(
          image = painterResource(id = R.drawable.hua_ji),
          text = stringResource(id = R.string.joinGroup),
          onClick = {
            withDrawerClosed {
              gotoArticlePage("H萌娘:官方群组")
            }
          }
        )
      }
    }
  }
}

@Composable
private fun Item(
  icon: ImageVector? = null,
  image: Painter? = null,
  text: String,
  onClick: () -> Unit,
) {
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .clickable { onClick() }
      .fillMaxWidth()
      .padding(vertical = 15.dp, horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      Icon(
        modifier = Modifier
          .width(28.dp)
          .height(28.dp),
        imageVector = icon,
        contentDescription = null,
        tint = themeColors.secondary
      )
    } else {
      Image(
        modifier = Modifier
          .size(28.dp),
        painter = image!!,
        contentDescription = null,
      )
    }

    StyledText(
      modifier = Modifier
        .padding(start = 20.dp),
      text = text,
      fontSize = 17.sp,
      color = themeColors.text.secondary,
      textAlign = TextAlign.Center
    )
  }
}