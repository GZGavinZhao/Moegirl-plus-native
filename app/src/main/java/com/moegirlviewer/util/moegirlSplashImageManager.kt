package com.moegirlviewer.util

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Environment
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.api.app.bean.HmoeSplashImageConfigBean
import com.moegirlviewer.api.app.bean.MoegirlSplashImageBean
import com.moegirlviewer.request.CommonRequestException
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.request.moeOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.time.LocalDate
import kotlin.math.absoluteValue

private const val configFileName = "config.json"

@SuppressLint("UseCompatLoadingForDrawables")
object MoegirlSplashImageManager {
  private val rootDir = File(
    Globals.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
    "splashImages"
  )
  private val configFile = File(rootDir, configFileName)
  private lateinit var config: List<MoegirlSplashImageBean>

  val fallbackImage = Globals.context.getDrawable(R.mipmap.splash_fallback)!!

  init {
    if (rootDir.exists().not()) rootDir.mkdirs()
    if (configFile.exists()) {
      val configJson = configFile.readText()
      if (configJson != "") {
        config = Gson().fromJson(configJson, Array<MoegirlSplashImageBean>::class.java).toList()
      }
    } else {
      configFile.createNewFile()
    }
  }

  fun getRandomImage(): SplashImage {
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }

    val images = if (this::config.isInitialized) {
      config
        .asSequence()
        .map { it.url.localImageFileName() }
        .filter { localImagesMap.containsKey(it) }
        .map { DrawableWrapper.createFromPath(localImagesMap[it]!!.path) }
        .toList() + listOf(fallbackImage)
    } else {
      localImagesMap.values.map { DrawableWrapper.createFromPath(it.path) }
    }

    val drawable = if (images.isNotEmpty()) images.random()!! else fallbackImage
    return SplashImage.onlyUseInSplashScreen(drawable)
  }

  fun getLatestImage(): SplashImage {
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }

    val drawable = if (this::config.isInitialized) {
      val latestImageName = config.last().url.localImageFileName()
      localImagesMap[latestImageName]?.let { DrawableWrapper.createFromPath(it.path) } ?: fallbackImage
    } else {
      fallbackImage
    }

    return SplashImage.onlyUseInSplashScreen(drawable)
  }

  fun getImageList(): List<MoegirlSplashImage> {
    if (!this::config.isInitialized) return emptyList()
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }
    return config
      .filter { localImagesMap.containsKey(it.url.localImageFileName()) }
      .map {
        val imageName = it.url.localImageFileName()
        MoegirlSplashImage(
          imageData = DrawableWrapper.createFromPath(localImagesMap[imageName]!!.path)!!,
          title = it.title,
          author = it.author,
          key = it.key,
          season = it.season
        )
      }
  }

  // 判断配置中的图片是否已经全部下载完毕
  fun isImagesReady(): Boolean {
    if (!this::config.isInitialized) return false
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }
    return config.all { localImagesMap.containsKey(it.url.localImageFileName()) }
  }

  suspend fun loadConfig() {
    try {
      config = AppApi.getMoegirlSplashImageConfig()
      configFile.writeText(Gson().toJson(config))
    } catch (e: CommonRequestException) {
      printRequestErr(e, "萌百：读取启动屏图片配置失败")
    }
  }

  suspend fun syncImagesByConfig() {
    if (this::config.isInitialized.not()) {
      printPlainLog("萌百：同步启动屏图片，但没有找到config")
      return
    }

    val allReferencedImageUrls = config.map { it.url }

    // 下载线上配置新增的图片
    withContext(Dispatchers.IO)  {
      allReferencedImageUrls.forEach { imageUrl ->
        if (rootDir.existsChild(imageUrl.localImageFileName()).not()) {
          launch {
            val request = Request.Builder()
              .url(imageUrl)
              .build()
            val res = try {
              commonOkHttpClient.newCall(request).execute()
            } catch (e: CommonRequestException) {
              printRequestErr(e, "萌百：启动屏图片下载失败：$imageUrl")
              return@launch
            }

            if (!res.isSuccessful) return@launch
            val imageByteArray = res.body!!.bytes()

            val file = File(rootDir, imageUrl.localImageFileName())
            file.createNewFile()
            file.writeBytes(imageByteArray)
            printPlainLog("萌百：启动屏图片下载完毕：$imageUrl")
          }
        }
      }
    }

    // 清除线上配置已经不存在的图片
    val allReferencedImageNames = allReferencedImageUrls.map { it.localImageFileName() }
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    localImages
      .filter { allReferencedImageNames.contains(it.name).not() }
      .forEach { it.deleteOnExit() }
  }
}

private fun String.localImageFileName() = computeMd5(this)