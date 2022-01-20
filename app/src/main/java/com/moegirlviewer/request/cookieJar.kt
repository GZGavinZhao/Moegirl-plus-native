package com.moegirlviewer.request

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.moegirlviewer.util.Globals

val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(Globals.context))