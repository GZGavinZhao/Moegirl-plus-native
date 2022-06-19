package com.moegirlviewer.util

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Environment
import androidx.compose.runtime.*
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.api.app.bean.MoegirlSplashImageBean
import com.moegirlviewer.request.CommonRequestException
import com.moegirlviewer.request.commonOkHttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import java.io.File

private const val configFileName = "config.json"
private var localImages by mutableStateOf(emptyList<File>())

@SuppressLint("UseCompatLoadingForDrawables")
object MoegirlSplashImageManager {
  private val rootDir = File(
    Globals.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
    "splashImages"
  )
  private val configFile = File(rootDir, configFileName)
  private var config: List<MoegirlSplashImageBean>? = null
  const val fallbackImage = R.mipmap.splash_fallback

  val imageTotal get() = config?.size ?: 0
  var readyImageTotal by mutableStateOf(0)

  init {
    initConfig()
    reloadLocalImages()
  }

  private fun initConfig() {
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

  private fun reloadLocalImages() {
    localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!.toList()
  }

  fun checkImageSyncStatus() {
    if (localImages.isEmpty() || config == null) return
    readyImageTotal = config!!.fold(0) { result, item ->
      val localImageFileName = item.url.localImageFileName()
      result + if (localImages.any { it.name == localImageFileName }) 1 else 0
    }
  }

  suspend fun getRandomImage(): SplashImage = withContext(Dispatchers.IO) {
    val usableImages: List<Any> = if (config != null) {
      config!!
        .asSequence()
        .map { it.url.localImageFileName() }
        .filter { imageNameInOnlineConfig -> localImages.any { it.name == imageNameInOnlineConfig } }
        .toList() + listOf(fallbackImage)
    } else {
      localImages
    }

    val randomImage = usableImages.randomOrNull() ?: fallbackImage
    SplashImage.onlyUseInSplashScreen(randomImage)
  }

  suspend fun getLatestImage(): SplashImage = withContext(Dispatchers.IO) {
    val latestImageName = if (config != null) {
      config!!.last().url.localImageFileName()
    } else ""
    val latestImage = localImages.firstOrNull { it.name == latestImageName } ?: fallbackImage

    SplashImage.onlyUseInSplashScreen(latestImage)
  }

  suspend fun getImageList(): List<MoegirlSplashImage> = withContext(Dispatchers.IO) {
    if (config == null) return@withContext emptyList()
    val localImagesMap = localImages.associateBy { it.name }
    config!!
      .filter { localImagesMap.containsKey(it.url.localImageFileName()) }
      .map {
        val imageName = it.url.localImageFileName()
        MoegirlSplashImage(
          imageData = localImagesMap[imageName]!!,
          title = it.title,
          author = it.author,
          key = it.key,
          season = it.season
        )
      }
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
    if (config == null) {
      printPlainLog("萌百：同步启动屏图片，但没有找到config")
      return@withContext
    }

    val allReferencedImageUrls = config!!.map { it.url }

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
          readyImageTotal++
          reloadLocalImages()
          printPlainLog("萌百：启动屏图片下载完毕：$imageUrl")
        }
      }
      .forEach { it.join() }

    printPlainLog("萌百：启动屏图片全部下载完毕")

    // 清除线上配置已经不存在的图片
    val allReferencedImageNames = allReferencedImageUrls.map { it.localImageFileName() }
    localImages
      .filter { allReferencedImageNames.contains(it.name).not() }
      .forEach { it.deleteOnExit() }
    printPlainLog("萌百：无用启动屏图片清理完毕")

    checkImageSyncStatus()
  }
}

private fun String.localImageFileName() = computeMd5(this)

@Composable
fun rememberMoegirlSplashImageList(): List<MoegirlSplashImage> {
  var reversedSplashImageList by remember { mutableStateOf(emptyList<MoegirlSplashImage>()) }

  LaunchedEffect(localImages) {
    reversedSplashImageList = MoegirlSplashImageManager.getImageList().reversed()
  }

  return reversedSplashImageList
}