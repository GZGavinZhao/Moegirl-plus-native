package com.moegirlviewer.screen.compare.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.R
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.compare.util.LinearDiffRows
import com.moegirlviewer.screen.compare.util.LinearDiffType
import com.moegirlviewer.theme.GreenPrimary
import com.moegirlviewer.theme.RedAccent
import com.moegirlviewer.theme.text

@Composable
fun CompareScreenPageDiffRows(data: LinearDiffRows) {
  val themeColors = MaterialTheme.colors
  var isExpanded by rememberSaveable { mutableStateOf(true) }

  Column() {
    // header
    Row(
      modifier = Modifier
        .clickable { isExpanded = !isExpanded }
        .height(40.dp)
        .fillMaxWidth()
        .background(themeColors.primaryVariant.copy(0.1f))
        .padding(horizontal = 10.dp)
      ,
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      @Composable
      fun lineNumberText(line: Int) = stringResource(id = R.string.lineNumber, line.toString())
      StyledText(
        text = lineNumberText(data.range.first) +
          if (data.range.first != data.range.last) " ~ ${lineNumberText(data.range.last)}" else "",
        color = themeColors.primaryVariant,
        fontSize = 18.sp,
      )

      Icon(
        modifier = Modifier
          .size(24.dp),
        imageVector = if (isExpanded)
          Icons.Filled.ExpandLess else
          Icons.Filled.ExpandMore,
        contentDescription = null,
        tint = themeColors.primaryVariant
      )
    }

    // list
    AnimatedVisibility(
      visible = isExpanded,
      enter = expandVertically(
        expandFrom = Alignment.Top
      ),
      exit = shrinkVertically(
        shrinkTowards = Alignment.Top
      )
    ) {
      SelectionContainer {
        StyledText(
          modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp),
          text = buildAnnotatedString {
            for (item in data.sentences) {
              withStyle(SpanStyle(
                background = when (item.type) {
                  LinearDiffType.CONTEXT -> Color.Transparent
                  LinearDiffType.ADDED -> GreenPrimary.copy(0.3f)
                  LinearDiffType.DELETED -> RedAccent.copy(0.3f)
                },
                textDecoration = if (item.type == LinearDiffType.DELETED) TextDecoration.LineThrough else null,
              )) {
                append(item.text)
              }
            }
          }
        )
      }
    }
  }
}