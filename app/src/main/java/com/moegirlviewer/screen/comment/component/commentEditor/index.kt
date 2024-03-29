package com.moegirlviewer.screen.comment.component.commentEditor

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.moegirlviewer.R
import com.moegirlviewer.api.edit.EditApi
import com.moegirlviewer.component.BackHandler
import com.moegirlviewer.component.Center
import com.moegirlviewer.component.PlainTextField
import com.moegirlviewer.component.ReloadButton
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewState
import com.moegirlviewer.component.styled.StyledCircularProgressIndicator
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.edit.EditScreenModel
import com.moegirlviewer.theme.text
import com.moegirlviewer.util.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@FlowPreview
@Composable
fun CommentEditor(
  visible: Boolean,
  value: String,
  useWikitext: Boolean,
  title: String,
  placeholder: String,
  onTextChange: ((text: String) -> Unit)? = null,
  onUseWikitextChange: (Boolean) -> Unit,
  onSubmit: () -> Unit,
  onDismiss: () -> Unit,
) {
  val model: EditScreenModel = hiltViewModel()
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  val cachedWebViews = rememberCachedWebViews()
  val articleViewState = remember { ArticleViewState() }
  val pagerState = rememberPagerState()
  var statusOfCommentPreview by remember { mutableStateOf(LoadStatus.INITIAL) }
  var commentPreviewHtml by remember { mutableStateOf("") }

  val transition = updateTransition(visible)
  val height = 150

  fun <T> animation() = tween<T>(
    durationMillis = 300
  )

  val topOffset by transition.animateDp({ animation() }) {
    if (it) 0.dp else height.dp
  }

  val maskAlpha by transition.animateFloat({ animation() }) {
    if (it) 0.5f else 0f
  }

  fun loadCommentPreviewHtml() = scope.launch {
    statusOfCommentPreview = LoadStatus.LOADING
    try {
      val res = EditApi.getPreview(value, "Mainpage")
      commentPreviewHtml = res.parse.text._asterisk
      statusOfCommentPreview = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "获取代码评论预览失败")
      statusOfCommentPreview = LoadStatus.FAIL
    }
  }

  LaunchedEffect(commentPreviewHtml) {
    articleViewState.updateHtmlView()
  }

  LaunchedEffect(useWikitext) {
    if (!useWikitext) pagerState.animateScrollToPage(0)
  }

  // 通过判断commentEditor是否收起，手动关闭输入法
  val composeBaseView = LocalView.current
  LaunchedEffect(topOffset == height.dp) {
    if (topOffset == height.dp) closeKeyboard()
  }

  BackHandler(visible) {
    onDismiss()
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    if (maskAlpha != 0f) {
      Spacer(modifier = Modifier
        .fillMaxSize()
        .background(Color(0f, 0f, 0f, maskAlpha))
        .noRippleClickable { onDismiss() }
      )
    }

    if (topOffset != height.dp) {
      Box(
        modifier = Modifier
          .imeBottomPadding()
          .noRippleClickable { }
          .fillMaxWidth()
          .height(height.dp)
          .offset(y = topOffset)
          .clip(
            RoundedCornerShape(
              topStart = 10.dp,
              topEnd = 10.dp
            )
          )
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(vertical = 10.dp, horizontal = 15.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row() {
              Crossfade(targetState = useWikitext) {
                Icon(
                  modifier = Modifier
                    .size(20.dp)
                    .noRippleClickable {
                      onUseWikitextChange(!useWikitext)
                      if (!useWikitext) toast(Globals.context.getString(R.string.useWikitext))
                    },
                  imageVector = if (it) Icons.Filled.Code else Icons.Filled.CodeOff,
                  contentDescription = null,
                  tint = themeColors.text.secondary
                )
              }

              AnimatedVisibility(
                visible = useWikitext,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
              ) {
                Crossfade(targetState = pagerState.currentPage == 0) {
                  Icon(
                    modifier = Modifier
                      .padding(start = 10.dp)
                      .size(20.dp)
                      .noRippleClickable {
                        scope.launch {
                          if (it) {
                            loadCommentPreviewHtml()
                            toast(Globals.context.getString(R.string.previewResult))
                          }
                          pagerState.animateScrollToPage(if (it) 1 else 0)
                        }
                      },
                    imageVector = if (it) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = themeColors.text.secondary
                  )
                }
              }
            }

            Box(
              modifier = Modifier
                .height(25.dp)
                .noRippleClickable { onSubmit() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                modifier = Modifier
                  .size(25.dp),
                imageVector = Icons.Filled.Send,
                contentDescription = null,
                tint = themeColors.secondary
              )
            }
          }

          Box(
            modifier = Modifier
              .padding(top = 3.dp)
              .weight(1f)
          ) {
            cachedWebViews.Provider {
              HorizontalPager(
                state = pagerState,
//                dragEnabled = false
                count = 2,
                userScrollEnabled = false
              ) { currentPage ->
                if (currentPage == 0) {
                  PlainTextField(
                    modifier = Modifier
                      .fillMaxSize()
                      .autoFocus(),
                    value = value,
                    placeholder = placeholder,
                    textStyle = TextStyle(
                      fontSize = 16.sp
                    ),
                    onValueChange = { onTextChange?.invoke(it) },
                  )
                } else {
                  Box() {
                    ArticleView(
                      state = articleViewState,
                      inDialogMode = true,
                      html = commentPreviewHtml,
                      editAllowed = false,
                      addCategories = false,
                    )

                    if (statusOfCommentPreview != LoadStatus.SUCCESS) {
                      Center(
                        modifier = Modifier
                          .fillMaxWidth()
                          .absoluteOffset(0.dp, 0.dp)
                          .background(themeColors.surface)
                      ) {
                        when(statusOfCommentPreview) {
                          LoadStatus.LOADING -> StyledCircularProgressIndicator()
                          LoadStatus.FAIL -> {
                            ReloadButton(
                              modifier = Modifier
                                .matchParentSize(),
                              onClick = { loadCommentPreviewHtml() }
                            )
                          }
                          else -> {}
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private data class State(
  val visible: Boolean = false,
  val title: String = "",
  val placeholder: String = "",
  val onSubmit: ((text: String, useWikitext: Boolean) -> Unit)? = null,
  val onDismiss: (() -> Unit) = {}
)

@FlowPreview
@ExperimentalPagerApi
@Composable
fun useCommentEditor(): CommentEditorSet {
  var state by remember { mutableStateOf(State()) }
  var inputVal by remember { mutableStateOf("") }
  var useWikitext by remember { mutableStateOf(false) }

  val controller = remember { object : CommentEditorController() {
      override fun show(
        targetName: String,
        isReply: Boolean,
        onSubmit: (inputVal: String, useWikitext: Boolean) -> Unit,
      ) {
        val title = Globals.context.getString(
          if (isReply) R.string.reply else R.string.comment
        )
        state = State(
          visible = true,
          title = title,
          placeholder = "$title：$targetName",
          onSubmit = { text, useWikitext ->
            onSubmit(text, useWikitext)
          },
        )
      }

      override fun hide() {
        state = state.copy(visible = false)
      }

      override fun clearInputVal() {
        inputVal = ""
      }
    }
  }

  return CommentEditorSet(controller) {
    CommentEditor(
      visible = state.visible,
      title = state.title,
      value = inputVal,
      useWikitext = useWikitext,
      placeholder = state.placeholder,
      onTextChange = { inputVal = it },
      onSubmit = { state.onSubmit?.invoke(inputVal, useWikitext) },
      onUseWikitextChange = { useWikitext = it },
      onDismiss = {
        state = state.copy(visible = false)
        state.onDismiss()
      }
    )
  }
}

abstract class CommentEditorController {
  abstract fun show(
    targetName: String,
    isReply: Boolean = false,
    onSubmit: (inputVal: String, useWikitext: Boolean) -> Unit
  )

  abstract fun hide()
  abstract fun clearInputVal()
}

data class CommentEditorSet(
  val controller: CommentEditorController,
  val Holder: @Composable () -> Unit
)

class CommentEditorExceptionByUserClosed : Exception()