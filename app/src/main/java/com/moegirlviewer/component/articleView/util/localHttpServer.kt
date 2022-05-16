package com.moegirlviewer.component.articleView.util

import android.annotation.SuppressLint
import com.moegirlviewer.R
import com.moegirlviewer.request.moeOkHttpClient
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.printDebugLog
import com.moegirlviewer.util.printRequestErr
import com.moegirlviewer.util.readAllBytes
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.*
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.BindException
import java.net.URLDecoder
import java.util.stream.Collectors

object LocalHttpServer {
  var port = (10000..87167).random()
  val host = "0.0.0.0"
  val rootUrl get() = "http://$host:$port"
  private var server: ApplicationEngine? = null

  fun stop() {
    server?.stop(1000, 1000)
    server = null
  }

  @OptIn(EngineAPI::class)
  @SuppressLint("ResourceType")
  fun start(port: Int = (10000..87167).random()) {
    if (server != null) return
    this.port = port
    fun startEmbeddedServer() = embeddedServer(CIO,
      host = host,
      port = port
    ) {
      routing {
        get ("/") {
          call.respondText("")
        }

        get("/main.js") {
          val fileContent = mainJsCompletableDeferred.await()
          call.respondText(fileContent, ContentType.parse("text/javascript"))
        }

        get("/main.css") {
          val fileContent = mainCssCompletableDeferred.await()
          call.respondText(fileContent, ContentType.parse("text/css"))
        }

//        get("/mmd-previewer-worker.js") {
//          val inputStream = Globals.context.assets.open("mmd-previewer-worker.js")
//          val result: String = BufferedReader(InputStreamReader(inputStream))
//            .lines().parallel().collect(Collectors.joining("\n"))
//          call.respondText(result, ContentType.parse("text/javascript"))
//        }

        get("/font/nospz_gothic_moe.ttf") {
          val fileContent = nospz_gothic_moeFontCompletableDeferred.await()
          call.respondBytes(fileContent)
        }

        get("/commonRes/{path}") {
          val resPath = URLDecoder.decode(call.parameters["path"]!!, "UTF-8")
          val okhttpRequest = Request.Builder()
            .url(resPath)
            .build()

          val res = try {
            moeOkHttpClient.newCall(okhttpRequest).execute()
          } catch (e: Exception) {
            printRequestErr(e, "宿主代理webView加载资源失败")
            return@get call.respond(404)
          }

          val byteStream = res.body?.bytes()!!
          call.respondBytes(byteStream)
        }
      }
    }.start(false)

    // 防止端口冲突
    try {
      server = startEmbeddedServer()
    } catch (e: BindException) {
      start()
    }
  }
}

private val coroutineScope = CoroutineScope(Dispatchers.Default)
private val mainJsCompletableDeferred = CompletableDeferred<String>().apply {
  coroutineScope.launch {
    val inputStream = Globals.context.assets.open("main.js")
    val result: String = BufferedReader(InputStreamReader(inputStream))
      .lines().parallel().collect(Collectors.joining("\n"))
    complete(result)
  }
}

private val mainCssCompletableDeferred = CompletableDeferred<String>().apply {
  coroutineScope.launch {
    val inputStream = Globals.context.assets.open("main.css")
    val result: String = BufferedReader(InputStreamReader(inputStream))
      .lines().parallel().collect(Collectors.joining("\n"))
    complete(result)
  }
}

@SuppressLint("ResourceType")
private val nospz_gothic_moeFontCompletableDeferred = CompletableDeferred<ByteArray>().apply {
  coroutineScope.launch {
    val inputStream = Globals.context.resources.openRawResource(R.font.nospz_gothic_moe)
     complete(inputStream.readAllBytes())
  }
}