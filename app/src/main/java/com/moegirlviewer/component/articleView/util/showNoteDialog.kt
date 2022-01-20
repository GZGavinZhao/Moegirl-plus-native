package com.moegirlviewer.component.articleView.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.MemoryStore
import com.moegirlviewer.component.articleView.ArticleView
import com.moegirlviewer.component.articleView.ArticleViewProps
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.util.CachedWebViews
import com.moegirlviewer.util.Globals

fun showNoteDialog(
  content: String
) {
  Globals.commonAlertDialog.show(CommonAlertDialogProps(
    title = Globals.context.getString(R.string.note),
    content = {
      val configuration = LocalConfiguration.current
      val memoryStore = remember { MemoryStore() }
      val cachedWebViews = remember { CachedWebViews() }

      DisposableEffect(true) {
        onDispose { cachedWebViews.destroyAllInstance() }
      }

      memoryStore.Provider {
        cachedWebViews.Provider {
          Box(
            modifier = Modifier
              .height((configuration.screenHeightDp * 0.2).dp)
          ) {
            ArticleView(props = ArticleViewProps(
              inDialogMode = true,
              html = content,
            ))
          }
        }
      }
    }
  ))
}