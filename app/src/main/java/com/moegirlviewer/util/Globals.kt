package com.moegirlviewer.util

import com.moegirlviewer.room.AppDatabase
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.moegirlviewer.component.commonDialog.CommonAlertDialogRef
import com.moegirlviewer.component.commonDialog.CommonDatePickerDialogState
import com.moegirlviewer.component.commonDialog.CommonLoadingDialogRef

// 专门存放全局延后赋值的变量
@SuppressLint("StaticFieldLeak")
object Globals {
  lateinit var activity: ComponentActivity
  lateinit var context: Context
  var statusBarHeight: Float = 24F
  lateinit var navController: NavHostController
  lateinit var commonAlertDialog: CommonAlertDialogRef
  lateinit var commonAlertDialog2: CommonAlertDialogRef
  lateinit var commonLoadingDialog: CommonLoadingDialogRef
  lateinit var commonDatePickerDialog: CommonDatePickerDialogState
  lateinit var room: AppDatabase
  lateinit var imageLoader: ImageLoader
  lateinit var httpUserAgent: String
}