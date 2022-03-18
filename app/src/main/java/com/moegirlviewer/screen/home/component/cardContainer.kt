package com.moegirlviewer.screen.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.noRippleClickable

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CardContainer(
  icon: ImageVector,
  title: String,
  minHeight: Dp = 300.dp,
  moreLink: MoreLink? = null,
  loadStatus: LoadStatus? = null,
  onClick: (() -> Unit)? = null,
  onReload: (() -> Unit)? = null,
  rightContent: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  val themeColors = MaterialTheme.colors
  val density = LocalDensity.current

  Column(
    modifier = Modifier
      .padding(15.dp)
      .fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          modifier = Modifier
            .size(density.run { 25.sp.toDp() }),
          imageVector = icon,
          contentDescription = null,
          tint = themeColors.secondary
        )

        StyledText(
          modifier = Modifier
            .padding(start = 10.dp),
          text = title,
          fontSize = 20.sp,
          color = themeColors.secondary,
          fontWeight = FontWeight.Bold
        )
      }

      if (moreLink != null) {
        StyledText(
          modifier = Modifier
            .noRippleClickable { moreLink.onClick() },
          text = moreLink.title + " →",
          color = themeColors.secondary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      } else {
        rightContent?.invoke()
      }
    }

    Card(
      modifier = Modifier
        .defaultMinSize(
          minHeight = minHeight
        )
        .padding(top = 15.dp)
        .fillMaxWidth(),
      elevation = 2.dp,
      shape = RoundedCornerShape(10.dp),
      onClick = { onClick?.invoke() }
    ) {
      Box(
        modifier = Modifier
          .animateContentSize()
      ) {
        content()

        if (loadStatus != null) {
          this@Column.AnimatedVisibility(
            modifier = Modifier
              .matchParentSize(),
            visible = loadStatus != LoadStatus.SUCCESS,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(themeColors.background.copy(alpha = 0.8f)),
              contentAlignment = Alignment.Center
            ) {
              if (loadStatus == LoadStatus.LOADING || loadStatus == LoadStatus.INIT_LOADING) {
                StyledCircularProgressIndicator()
              }

              if (loadStatus == LoadStatus.FAIL) {
                ReloadButton(
                  modifier = Modifier
                    .matchParentSize(),
                  onClick = { onReload?.invoke() }
                )
              }
            }
          }
        }
      }
    }
  }
}

class MoreLink(
  val title: String,
  val onClick: () -> Unit
)