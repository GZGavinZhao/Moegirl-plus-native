package com.moegirlviewer.util

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Environment
import androidx.compose.runtime.*
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.api.app.bean.HmoeSplashImageConfigBean
import com.moegirlviewer.api.app.bean.MoegirlSplashImageBean
import com.moegirlviewer.request.CommonRequestException
import com.moegirlviewer.request.commonOkHttpClient
import com.moegirlviewer.request.moeOkHttpClient
import com.moegirlviewer.request.send
import kotlinx.coroutines.*
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

  suspend fun getRandomImage(): SplashImage = withContext(Dispatchers.IO) {
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }
    val pathPlaceholderOfFallbackImage = "FALLBACK"

    val imagePaths = if (this@MoegirlSplashImageManager::config.isInitialized) {
      config
        .asSequence()
        .map { it.url.localImageFileName() }
        .filter { localImagesMap.containsKey(it) }
        .toList() + listOf(pathPlaceholderOfFallbackImage)
    } else {
      localImagesMap.values.map { it.path }
    }

    val randomImagePath = if (imagePaths.isNotEmpty()) imagePaths.random() else pathPlaceholderOfFallbackImage
    val randomImageDrawable = if (randomImagePath == pathPlaceholderOfFallbackImage)
      fallbackImage else
      Drawable.createFromPath(randomImagePath)!!

    SplashImage.onlyUseInSplashScreen(randomImageDrawable)
  }

  suspend fun getLatestImage(): SplashImage = withContext(Dispatchers.IO) {
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }

    val drawable = if (this@MoegirlSplashImageManager::config.isInitialized) {
      val latestImageName = config.last().url.localImageFileName()
      localImagesMap[latestImageName]?.let { DrawableWrapper.createFromPath(it.path) } ?: fallbackImage
    } else {
      fallbackImage
    }

    SplashImage.onlyUseInSplashScreen(drawable)
  }

  private var imageList: List<MoegirlSplashImage>? = null
  suspend fun getImageList(): List<MoegirlSplashImage> = withContext(Dispatchers.IO) {
    if (!this@MoegirlSplashImageManager::config.isInitialized) return@withContext emptyList()
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    if (imageList != null) return@withContext imageList!!
    val localImagesMap = localImages.associateBy { it.name }
    config
      .filter { localImagesMap.containsKey(it.url.localImageFileName()) }
      .map {
        val imageName = it.url.localImageFileName()
        async {
          MoegirlSplashImage(
            imageData = DrawableWrapper.createFromPath(localImagesMap[imageName]!!.path)!!,
            title = it.title,
            author = it.author,
            key = it.key,
            season = it.season
          )
        }
      }
      .map { it.await() }
      .also { if (config.size == it.size) imageList = it }
  }

  // 判断是否至少有一张已下载的图片
  fun isImagesReady(): Boolean {
    if (!this::config.isInitialized) return false
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }
    return localImagesMap.isNotEmpty()
  }

  suspend fun loadConfig() = withContext(Dispatchers.IO) {
    try {
      config = AppApi.getMoegirlSplashImageConfig()
      configFile.writeText(Gson().toJson(config))
    } catch (e: CommonRequestException) {
      printRequestErr(e, "萌百：读取启动屏图片配置失败")
    }
  }

  suspend fun syncImagesByConfig() = withContext(Dispatchers.IO) {
    if (this@MoegirlSplashImageManager::config.isInitialized.not()) {
      printPlainLog("萌百：同步启动屏图片，但没有找到config")
      return@withContext
    }

    val allReferencedImageUrls = config.map { it.url }

    // 下载线上配置新增的图片
    allReferencedImageUrls
      .filter { imageUrl -> rootDir.existsChild(imageUrl.localImageFileName()).not() }
      .map { imageUrl ->
        launch {
          val request = Request.Builder()
            .url(imageUrl)
            .build()
          val res = try {
            commonOkHttpClient.newCall(request).execute()
          } catch (e: Exception) {
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
      .forEach { it.join() }

    printPlainLog("萌百：启动屏图片全部下载完毕")

    // 清除线上配置已经不存在的图片
    val allReferencedImageNames = allReferencedImageUrls.map { it.localImageFileName() }
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    localImages
      .filter { allReferencedImageNames.contains(it.name).not() }
      .forEach { it.deleteOnExit() }
    printPlainLog("萌百：无用启动屏图片清理完毕")
  }
}

private fun String.localImageFileName() = computeMd5(this)

@Composable
fun rememberMoegirlSplashImageList(): List<MoegirlSplashImage> {
  var reversedSplashImageList by remember { mutableStateOf(emptyList<MoegirlSplashImage>()) }

  LaunchedEffect(true) {
    reversedSplashImageList = MoegirlSplashImageManager.getImageList()
  }

  return reversedSplashImageList
}