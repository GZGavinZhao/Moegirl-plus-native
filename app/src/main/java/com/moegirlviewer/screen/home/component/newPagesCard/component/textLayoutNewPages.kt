package com.moegirlviewer.screen.home.component.newPagesCard.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moegirlviewer.api.editingRecord.bean.NewPagesBean
import com.moegirlviewer.api.page.bean.PageProfileBean
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.util.gotoArticlePage
import kotlin.math.min

@Composable
fun TextLayoutNewPages(
  pageList: List<PageProfileBean.Query.MapValue>
) {
  val themeColors = MaterialTheme.colors

  val annotatedString = buildAnnotatedString {
    for ((index, item) in pageList.take(40).withIndex()) {
      withStyle(SpanStyle(color = themeColors.primaryVariant)) {
        pushStringAnnotation("link", item.title)
        append(item.title)
        pop()
      }
      if (index != min(39, pageList.size - 1)) append("、")
    }
  }

  StyledText(
    modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp),
    text = annotatedString,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    onClick = { offset ->
      annotatedString.getStringAnnotations("link", offset, offset).firstOrNull()?.let {
        gotoArticlePage(it.item)
      }
    }
  )
}