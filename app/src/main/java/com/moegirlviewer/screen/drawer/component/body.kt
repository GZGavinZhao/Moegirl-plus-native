package com.moegirlviewer.screen.drawer.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.article.ArticleRouteArguments
import com.moegirlviewer.screen.contribution.ContributionRouteArguments
import com.moegirlviewer.screen.drawer.CommonDrawerState
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.launch

@Composable
fun CommonDrawerBody(
  modifier: Modifier = Modifier,
  commonDrawerState: CommonDrawerState
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()
  val isLoggedIn by AccountStore.isLoggedIn.collectAsState(initial = false)
  val userNameOfCurrentAccount by AccountStore.userName.collectAsState(initial = "")

  fun withDrawerClosed(exec: () -> Unit) = scope.launch {
    launch { commonDrawerState.close() }
    exec()
  }

  fun gotoRandomPage() = scope.launch {
    try {
      Globals.commonLoadingDialog.showText(Globals.context.getString(R.string.doingRandom) + "...")
      val randomPage = PageApi.getRandomPage().query.pages.values.first().title
      gotoArticlePage(randomPage)
    } catch (e: MoeRequestException) {
      toast(e.message)
    } finally {
      Globals.commonLoadingDialog.hide()
    }
  }

  RippleColorScope(themeColors.primaryVariant) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .then(modifier)
    ) {
      Item(
        icon = ImageVector.vectorResource(R.drawable.dice_5),
        text = stringResource(id = R.string.randomArticle),
        onClick = {
          withDrawerClosed { gotoRandomPage() }
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
        icon = Icons.Filled.Forum,
        text = stringResource(id = R.string.talkPage),
        onClick = {
          withDrawerClosed {
            Globals.navController.navigate(ArticleRouteArguments(
              pageKey = PageNameKey(isMoegirl("萌娘百科 talk:讨论版", "H萌娘讨论:讨论版"))
            ))
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
          icon = Icons.Filled.Group,
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
        tint = themeColors.primaryVariant
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