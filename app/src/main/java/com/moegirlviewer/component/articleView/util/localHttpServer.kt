package com.moegirlviewer.component.articleView.util
//
//import android.annotation.SuppressLint
//import com.moegirlviewer.R
//import com.moegirlviewer.util.Globals
//import com.moegirlviewer.util.readAllBytes
//import io.ktor.application.*
//import io.ktor.http.*
//import io.ktor.response.*
//import io.ktor.routing.*
//import io.ktor.server.cio.*
//import io.ktor.server.engine.*
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.util.stream.Collectors

// 暂时没用了
//object LocalHttpServer {
//  val port = 8080
//  val host = "0.0.0.0"
//  val rootUrl = "http://$host:$port"
//
//  @SuppressLint("ResourceType")
//  fun start() {
//    embeddedServer(CIO,
//      host = host,
//      port = port
//    ) {
//      routing {
//        get ("/") {
//          call.respondText("hello")
//        }
//
//        get("/main.js") {
//          val inputStream = Globals.context.assets.open("main.js")
//          val result: String = BufferedReader(InputStreamReader(inputStream))
//            .lines().parallel().collect(Collectors.joining("\n"))
//          call.respondText(result, ContentType.parse("text/javascript"))
//        }
//
//        get("/main.css") {
//          val inputStream = Globals.context.assets.open("main.css")
//          val result: String = BufferedReader(InputStreamReader(inputStream))
//            .lines().parallel().collect(Collectors.joining("\n"))
//          call.respondText(result, ContentType.parse("text/css"))
//        }
//
//        get("/font/nospz_gothic_moe.ttf") {
//          val inputStream = Globals.context.resources.openRawResource(R.font.nospz_gothic_moe)
//          call.respondBytes(inputStream.readAllBytes())
//        }
//      }
//    }.start(false)
//  }
//}