package com.moegirlviewer.screen.search.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.R
import com.moegirlviewer.component.BackButton
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.styled.StyledTopAppBar
import com.moegirlviewer.screen.search.SearchScreenModel
import com.moegirlviewer.store.SearchRecord
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.toast

@Composable
fun SearchScreenHeader() {
  val model: SearchScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val context = LocalContext.current
  val bgColor = if (themeColors.isLight) Color.White else themeColors.primary

  StyledTopAppBar(
    backgroundColor = bgColor,
    statusBarDarkIcons = themeColors.isLight,
    navigationIcon = {
      BackButton(
        iconColor = themeColors.text.secondary
      )
    },
    elevation = 3.dp,
    title = {
      PlainTextField(
        modifier = Modifier
          .fillMaxWidth(),
        value = model.keywordInputValue,
        singleLine = true,
        textStyle = TextStyle(
          fontSize = 16.sp,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
          onAny = {
            if (model.keywordInputValue.trim() == "") {
              toast(context.getString(R.string.emptySearchKeywordHint))
            } else {
              model.searchByRecord(SearchRecord(model.keywordInputValue, false))
            }
          }
        ),
        onValueChange = { model.keywordInputValue = it },
        placeholder = stringResource(id = R.string.searchPlaceholderText),
      )
    },
    actions = {
      if (model.keywordInputValue != "") {
        IconButton(
          onClick = { model.keywordInputValue = "" },
        ) {
          Icon(
            modifier = Modifier
              .width(24.dp)
              .height(24.dp),
            imageVector = Icons.Filled.Close,
            contentDescription = null
          )
        }
      }
    },
  )
}