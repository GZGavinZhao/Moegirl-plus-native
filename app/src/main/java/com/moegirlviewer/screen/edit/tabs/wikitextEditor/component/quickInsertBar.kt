package com.moegirlviewer.screen.edit.tabs.wikitextEditor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.ui.theme.text
import com.moegirlviewer.util.BorderSide
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.sideBorder

@ExperimentalComposeUiApi
@Composable
fun QuickInsertBar(
  onClickItem: (QuickInsertText) -> Unit
) {
  val themeColors = MaterialTheme.colors

  Row(
    modifier = Modifier
      .sideBorder(BorderSide.TOP, 1.dp, themeColors.text.tertiary)
      .fillMaxWidth()
      .background(themeColors.surface)
      .horizontalScroll(rememberScrollState())
  ) {
    Item(
      title = "[[ ]]",
      subtitle = stringResource(id = R.string.link),
      onClick = { onClickItem(QuickInsertText("[[]]", 2)) }
    )
    Item(
      title = "{{ }}",
      subtitle = stringResource(id = R.string.template),
      onClick = { onClickItem(QuickInsertText("{{}}", 2)) }
    )
    Item(
      title = "|",
      subtitle = stringResource(id = R.string.pipeChar),
      onClick = { onClickItem(QuickInsertText("|")) }
    )
    Item(
      icon = ImageVector.vectorResource(id = R.drawable.fountain_pen_tip),
      subtitle = stringResource(id = R.string.sign),
      onClick = { onClickItem(QuickInsertText("--~~~~")) }
    )
    Item(
      title = stringResource(id = R.string.strong),
      onClick = { onClickItem(QuickInsertText("''''''", 3)) }
    )
    Item(
      title = "<del>",
      subtitle = stringResource(id = R.string.delLine),
      onClick = { onClickItem(QuickInsertText("<del></del>", 6)) }
    )
    Item(
      title = stringResource(id = R.string.heimu),
      onClick = { onClickItem(QuickInsertText(
        "{{${Globals.context.getString(R.string.heimu)}|}}", 2
      )) }
    )
    Item(
      title = stringResource(id = R.string.colorText),
      onClick = { onClickItem(
        QuickInsertText(
          "{{color|${Globals.context.getString(R.string.colorTextPlaceholder)}}}", 5, 2
      )) }
    )
    Item(
      title = "#",
      subtitle = stringResource(id = R.string.list),
      onClick = { onClickItem(QuickInsertText("# ")) }
    )
    Item(
      title = stringResource(id = R.string.level2Title),
      onClick = { onClickItem(QuickInsertText("==  ==", 3)) }
    )
    Item(
      title = stringResource(id = R.string.level3Title),
      onClick = { onClickItem(QuickInsertText("===  ===", 4)) }
    )
  }
}

@ExperimentalComposeUiApi
@Composable
private fun Item(
  title: String? = null,
  icon: ImageVector? = null,
  subtitle: String? = null,
  onClick: () -> Unit
) {
  val themeColors = MaterialTheme.colors

  HoverBgColorButton(
    onClick = onClick
  ) {
    Column(
      modifier = Modifier
        .height(45.dp)
        .padding(horizontal = 15.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (title != null) {
        Text(
          text = title,
          color = themeColors.text.secondary,
        )
      } else {
        Icon(
          modifier = Modifier
            .size(22.dp),
          imageVector = icon!!,
          contentDescription = null,
          tint = themeColors.text.secondary
        )
      }

      if (subtitle != null) {
        Text(
          text = subtitle,
          fontSize = 9.sp,
          color = themeColors.text.secondary,
        )
      }
    }
  }
}

class QuickInsertText(
  val text: String,
  val minusOffset: Int = 0,
  val selectionMinusOffset: Int = 0
)

