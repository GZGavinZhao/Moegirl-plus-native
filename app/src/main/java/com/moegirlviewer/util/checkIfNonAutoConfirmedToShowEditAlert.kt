package com.moegirlviewer.util

import androidx.compose.material.Text
import com.moegirlviewer.R
import com.moegirlviewer.component.commonDialog.ButtonConfig
import com.moegirlviewer.component.commonDialog.CommonAlertDialogProps
import com.moegirlviewer.store.AccountStore
import com.moegirlviewer.store.UserGroup
import kotlinx.coroutines.CompletableDeferred

suspend fun checkIfNonAutoConfirmedToShowEditAlert(
  pageName: String,
  section: String? = null
): Boolean {
  val isAutoConfirmed = AccountStore.isInUserGroup(UserGroup.AUTO_CONFIRMED)
  if (!isAutoConfirmed) {
    val result = CompletableDeferred<Boolean>().apply {
      Globals.commonAlertDialog.show(CommonAlertDialogProps(
        onPrimaryButtonClick = {
          this.complete(true)
        },
        secondaryButton = ButtonConfig.cancelButton(
          onClick = { this.complete(false) }
        ),
        content = {
          Text(Globals.context.getString(R.string.nonAutoConfirmedHint))
        }
      ))
    }.await()

    if (result) {
      val sectionParam = if (section != null) "&section=$section" else ""
      openHttpUrl("https://mzh.moegirl.org.cn/index.php?title=$pageName&action=edit$sectionParam")
    }

    return true
  }

  return false
}