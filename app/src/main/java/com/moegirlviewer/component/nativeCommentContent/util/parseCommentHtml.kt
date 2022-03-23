package com.moegirlviewer.component.nativeCommentContent.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moegirlviewer.R
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.util.Globals
import com.moegirlviewer.util.commentEmotionList.getHmoeEmotionByName
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.net.URLDecoder

fun parseCommentHtml(
  html: String,
  density: Density,
  maxImageWidth: Int,
): List<CommentElement> {
  val htmlDoc = Jsoup.parse(html)
  val elements = htmlDoc.body().childNodes()

  val commentElements = mutableListOf<CommentElement>()

  fun traversal(elements: List<Node>) {
    for (item in elements) {
      when(item) {
        is TextNode -> commentElements.add(CommentText(item.text()))
        is Element -> {
          when {
            item.hasClass("heimu") -> {
              commentElements.add(CommentText(
                text = item.text(),
                spanStyle = SpanStyle(
                  background = Color.Black,
                  color = Color.White
                )
              ))
            }

            item.tagName() == "a" &&
            item.childNodeSize() == 1 &&
            item.childNode(0) is TextNode -> commentElements.add(aTagParser(item))

            item.tagName() == "img" -> {
              val widthAttr = item.attr("width")
              val heightAttr = item.attr("height")
              val imageUrl = item.attr("src").correctImgUrl()

              commentElements.add(createImageInlineContent(
                density = density,
                maxImageWidth = maxImageWidth,
                width = widthAttr.toIntOrNull(),
                height = heightAttr.toIntOrNull(),
                imageUrl = imageUrl
              ))
            }

            item.hasClass("emos") -> {
              val emotionName = item.classNames().first { it.contains("emoji") }
              val emotion = getHmoeEmotionByName(emotionName) ?: continue

              commentElements.add(createImageInlineContent(
                density = density,
                maxImageWidth = maxImageWidth,
                width = 20,
                height = 20,
                imageUrl = emotion.imageUrl
              ))
            }

            else -> if (item.childNodeSize() != 0) traversal(item.childNodes())
          }
        }
      }
    }
  }

  traversal(elements)
  return commentElements
}

sealed class CommentElement

class CommentText(
  val text: String,
  val spanStyle: SpanStyle = SpanStyle()
): CommentElement()

class CommentInlineContent(
  val id: String,
  val content: InlineTextContent
) : CommentElement()

class CommentLinkedText(
  val text: String,
  val target: String,
  val type: CommentLinkType
) : CommentElement()

class CommentCustomAnnotatedText(
  val text: String,
  val tag: String,
  val annotation: String,
  val textStyle: SpanStyle? = null
) : CommentElement()

enum class CommentLinkType(val textAnnoTag: String) {
  INTERNAL("__INTERNAL"),
  EXTERNAL("__EXTERNAL"),
  NEW("__NEW"),
  INVALID("__INVALID")
}

private class ImageSize(
  val width: Int = 0,
  val height: Int = 0
)

private fun aTagParser(tagElement: Element): CommentLinkedText {
  val href = tagElement.attr("href")
  val text = tagElement.text()
  return when {
    href == "" -> {
      CommentLinkedText(
        text = text,
        target = "",
        type = CommentLinkType.INVALID
      )
    }
    tagElement.hasClass("new") -> {
      CommentLinkedText(
        text = text,
        target = "https://xx.xx$href".toUri().getQueryParameter("title")!!,
        type = CommentLinkType.NEW
      )
    }
    href[0] == '/' -> {
      CommentLinkedText(
        text = text,
        target = URLDecoder.decode(href.substring(1), "utf8"),
        type = CommentLinkType.INTERNAL
      )
    }
    else -> CommentLinkedText(
      text = text,
      target = href,
      type = CommentLinkType.EXTERNAL
    )
  }
}

private fun probeImageSize(imageUrl: String): ImageSize {
  val completableDeferred = CompletableDeferred<ImageSize>()

  val request = ImageRequest.Builder(Globals.context)
    .data(imageUrl)
    .target(
      onSuccess = {
        completableDeferred.complete(
          ImageSize(
            width = it.intrinsicWidth,
            height = it.intrinsicHeight
          )
        )
      },
      onError = {
        completableDeferred.complete(ImageSize())
      }
    )
    .build()

  Globals.imageLoader.enqueue(request)

  return runBlocking { completableDeferred.await() }
}

private var incrementIdForInlineContent = 0

private fun createImageInlineContent(
  density: Density,
  width: Int? = null,
  height: Int? = null,
  maxImageWidth: Int,
  imageUrl: String
): CommentInlineContent {
  var finalWidth = width
  var finalHeight = height

  if (finalWidth == null || finalHeight == null) {
    val imageSize = probeImageSize(imageUrl)
    finalWidth = imageSize.width
    finalHeight = imageSize.height
  }


  if (finalWidth > maxImageWidth) {
    finalHeight = finalHeight * maxImageWidth / finalWidth
    finalWidth = maxImageWidth
  }

  val inlineContent = InlineTextContent(
    placeholder = Placeholder(
      width = density.run { finalWidth.dp.toSp() },
      height = density.run { finalHeight.dp.toSp() },
      placeholderVerticalAlign = PlaceholderVerticalAlign.Bottom
    ),
    children = {
      Box(
        modifier = Modifier
          .fillMaxSize()
      ) {
        AsyncImage(
          modifier = Modifier
            .fillMaxSize(),
          model = rememberImageRequest(imageUrl),
          contentDescription = null,
          placeholder = painterResource(R.drawable.placeholder)
        )
      }
    }
  )

  return CommentInlineContent(
    id = (++incrementIdForInlineContent).toString(),
    content = inlineContent
  )
}

private fun String.correctImgUrl(): String {
  return this.replace("https://img.moegirl.org/", "https://img.moegirl.org.cn/")
}