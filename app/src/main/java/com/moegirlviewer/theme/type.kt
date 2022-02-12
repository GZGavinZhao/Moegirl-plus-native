package com.moegirlviewer.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// 文字样式全由component/styled/styledText设置
val Typography = Typography(
  body1 = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
  ),
//button = TextStyle(
//  fontFamily = FontFamily.Default,
//  fontWeight = FontWeight.W500,
//  fontSize = 14.sp
//),
//caption = TextStyle(
//  fontFamily = FontFamily.Default,
//  fontWeight = FontWeight.Normal,
//  fontSize = 12.sp
//)
)