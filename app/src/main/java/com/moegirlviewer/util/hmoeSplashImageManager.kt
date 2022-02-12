package com.moegirlviewer.util

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Environment
import com.google.gson.Gson
import com.moegirlviewer.R
import com.moegirlviewer.api.app.AppApi
import com.moegirlviewer.api.app.bean.HmoeSplashImageConfigBean
import com.moegirlviewer.request.CommonRequestException
import com.moegirlviewer.request.commonOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.time.LocalDate
import kotlin.math.absoluteValue

private const val configFileName = "config.json"

@SuppressLint("UseCompatLoadingForDrawables")
object HmoeSplashImageManager {
  private val rootDir = File(
    Globals.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
    "splashImages"
  )
  private val configFile = File(rootDir, configFileName)
  private lateinit var config: HmoeSplashImageConfigBean

  init {
    if (rootDir.exists().not()) rootDir.mkdirs()
    if (configFile.exists()) {
      val configJson = configFile.readText()
      if (configJson != "") {
        config = Gson().fromJson(configJson, HmoeSplashImageConfigBean::class.java)
      }
    } else {
      configFile.createNewFile()
    }
  }

  fun getRandomImage(): Drawable {
    val localImages = rootDir.listFiles { _, fileName -> fileName != configFileName }!!
    val localImagesMap = localImages.associateBy { it.name }
    val fallbackImage = Globals.context.getDrawable(R.mipmap.splash_fallback)!!

    val images = if (this::config.isInitialized) {
      config.checkFestivalImages(localImagesMap)
        ?.map { DrawableWrapper.createFromPath(it) }
        ?: (
          config.images
            .asSequence()
            .filter { !it.disabled }
            .map { it.imageUrl.localImageFileName() }
            .filter { localImagesMap.containsKey(it) }
            .map { DrawableWrapper.createFromPath(localImagesMap[it]!!.path) }
            .toList() + listOf(fallbackImage)
        )
    } else {
      localImagesMap.values.map { DrawableWrapper.createFromPath(it.path) }
    }

    return if (images.isNotEmpty()) images.random()!! else fallbackImage
  }

  suspend fun loadConfig() {
    try {
      config = AppApi.getHmoeSplashImageConfig()
      configFile.writeText(Gson().toJson(config))
    } catch (e: CommonRequestException) {
      printRequestErr(e, "H萌娘：读取启动屏图片配置失败")
    }
  }

  suspend fun syncImagesByConfig() {
    if (this::config.isInitialized.not()) {
      printPlainLog("H萌娘：同步启动屏图片，但没有找到config")
      return
    }

    val allReferencedImageUrls = config.images.map { it.imageUrl } +
      config.festivals.flatMap { it.imageUrls }

    // 下载线上配置新增的图片
    allReferencedImageUrls.forEach { imageUrl ->
      if (rootDir.existsChild(imageUrl.localImageFileName()).not()) {
        val request = Request.Builder()
          .url(imageUrl)
          .build()
        withContext(Dispatchers.IO) {
          val res = try {
            commonOkHttpClient.newCall(request).execute()
          } catch (e: CommonRequestException) {
            printRequestErr(e, "H萌娘：启动屏图片下载失败：$imageUrl")
            return@withContext
          }

          if (!res.isSuccessful) return@withContext
          val imageByteArray = res.body!!.bytes()

          val file = File(rootDir, imageUrl.localImageFileName())
          file.createNewFile()
          file.writeBytes(imageByteArray)
          printPlainLog("H萌娘：启动屏图片下载完毕：$imageUrl")
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

private fun HmoeSplashImageConfigBean.checkFestivalImages(
  localImageFiles: Map<String, File>
): List<String>? {
  val localDate = LocalDate.now()
  val foundFestival = this.festivals.firstOrNull {
    val (month, date) = it.date.split("-")
    !it.disabled &&
      localDate.month.value == month.toInt() &&
      localDate.dayOfMonth.absoluteValue == date.toInt()
  }

  val localFestivalImagePaths = foundFestival?.imageUrls?.mapNotNull {
    val festivalImageLocalName = it.localImageFileName()
    localImageFiles[festivalImageLocalName]?.path
  }

  return if (localFestivalImagePaths != null && localFestivalImagePaths.isNotEmpty()) localFestivalImagePaths else null
}

private fun String.localImageFileName() = computeMd5(this)