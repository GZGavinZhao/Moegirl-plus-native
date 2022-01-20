// compose封装的DrawerLayout，暂时用不上了，抽屉使用CustomDrawer
package com.moegirlviewer.component.compose.composeDrawerLayout

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.coroutines.delay


open class ComposeDrawerLayoutRef(
  private val view: DrawerLayout,
  private val gravity: Int
) {
  suspend fun open() {
    view.openDrawer(gravity)
    delay(350)
  }

  suspend fun close() {
    view.closeDrawer(gravity)
    delay(350)
  }

  fun setLockMode(locked: ComposeDrawerLayoutLockMode) {
    view.setDrawerLockMode(locked.code)
  }
}

@Composable
fun ComposeDrawerLayout(
  width: Dp,
  gravity: ComposeDrawerLayoutGravity = ComposeDrawerLayoutGravity.LEFT,
  drawerContent: @Composable () -> Unit,
  ref: Ref<ComposeDrawerLayoutRef>? = null,
  content: @Composable () -> Unit
) {
  val density = LocalDensity.current

  // 反射设置滑动触发宽度
  fun setEdgeSize(drawerLayout: DrawerLayout, edgeSize: Int) {
    val mDragger = drawerLayout::class.java.getDeclaredField("mLeftDragger")
    mDragger.isAccessible = true
    val draggerObj = mDragger.get(drawerLayout)
    val mEdgeSize = draggerObj.javaClass.getDeclaredField("mEdgeSize")
    mEdgeSize.isAccessible = true
    mEdgeSize.setInt(draggerObj, edgeSize)
  }

  fun initView(context: Context): View {
    val fillSizeLayoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )

    val drawerLayoutView = DrawerLayout(context).apply {
      this.layoutParams = fillSizeLayoutParams
    }

    val drawerContentView = ComposeView(context).apply {
      this.layoutParams = DrawerLayout.LayoutParams(fillSizeLayoutParams).apply {
        this.width = density.run { width.roundToPx() }
        this.gravity = gravity.code
      }

      this.setContent { drawerContent() }
    }

    val contentView = ComposeView(context).apply {
      this.layoutParams = fillSizeLayoutParams
      this.setContent { content() }
    }

    setEdgeSize(drawerLayoutView, 70)

    return drawerLayoutView.apply {
      this.addView(contentView)
      this.addView(drawerContentView)
      this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

      ref?.value = ComposeDrawerLayoutRef(this, gravity.code)
    }
  }

  AndroidView(
    modifier = Modifier
      .clickable {},
    factory = { initView(it) }
  )
}

enum class ComposeDrawerLayoutGravity(val code: Int) {
  LEFT(Gravity.START),
  RIGHT(Gravity.END)
}

enum class ComposeDrawerLayoutLockMode(val code: Int) {
  LOCKED(DrawerLayout.LOCK_MODE_LOCKED_CLOSED),
  UNLOCKED(DrawerLayout.LOCK_MODE_UNLOCKED)
}