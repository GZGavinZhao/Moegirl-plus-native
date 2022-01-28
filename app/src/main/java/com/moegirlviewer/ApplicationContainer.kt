package com.moegirlviewer

import android.app.Application
import com.moegirlviewer.initialization.initializeOnCreate
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ApplicationContainer : Application()

