package com.moegirlviewer.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ParcelableRef<T : Parcelable>(
  var value: T
): Parcelable