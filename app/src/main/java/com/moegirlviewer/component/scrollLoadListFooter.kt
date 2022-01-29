package com.moegirlviewer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.LoadStatus
import com.moegirlviewer.util.noRippleClickable

@Composable
fun ScrollLoadListFooter(
  modifier: Modifier = Modifier,
  status: LoadStatus,
  errorText: String = stringResource(id = R.string.loadErrToClickRetry),
  allLoadedText: String = stringResource(id = R.string.noMore),
  emptyText: String = stringResource(id = R.string.noData),
  onReload: (() -> Unit)? = null,
) {
  val themeColors = MaterialTheme.colors
  val fontSize = 16.sp

  fun Modifier.defaultMargin(): Modifier {
    return this.padding(vertical = 20.dp)
  }

  @Composable
  fun Container(content: @Composable () -> Unit) {
    Box(
      modifier = Modifier
        .defaultMargin()
        .fillMaxWidth(),
      contentAlignment = Alignment.Center
    ) {
      content()
    }
  }

  when(status) {
    LoadStatus.LOADING -> {
      Container {
        StyledCircularProgressIndicator()
      }
    }
    LoadStatus.ALL_LOADED -> {
      Container {
        StyledText(
          text = allLoadedText,
          color = themeColors.text.secondary,
          fontSize = fontSize
        )
      }
    }
    LoadStatus.EMPTY -> {
      Container {
        StyledText(
          text = emptyText,
          color = themeColors.text.secondary,
          fontSize = fontSize
        )
      }
    }
    LoadStatus.FAIL -> {
      Container {
        StyledText(
          modifier = Modifier
            .noRippleClickable { onReload?.invoke() },
          text = errorText,
          color = themeColors.text.secondary,
          fontSize = fontSize
        )
      }
    }
    else -> {}
  }
}