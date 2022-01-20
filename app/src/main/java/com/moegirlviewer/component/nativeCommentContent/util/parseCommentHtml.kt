package com.moegirlviewer.component.nativeCommentContent.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.moegirlviewer.util.Globals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

fun parseCommentHtml(
  html: String,
  density: Density,
  maxImageWidth: Int,
): List<CommentElement> {
  val htmlDoc = Jsoup.parse(html)
  val elements = htmlDoc.body().childNodes()

  val commentElements = mutableListOf<CommentElement>()
  var incrementIdForInlineContent = 0

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

            item.tagName() == "img" -> {
              var widthAttr = item.attr("width")
              var heightAttr = item.attr("height")
              val imageUrl = item.attr("src")

              // 如果img标签上没有width或height属性，这里尝试手动获取一下大小
              if (widthAttr == "" || heightAttr == "") {
                val imageSize = probeImageSize(imageUrl)
                widthAttr = imageSize.width.toString()
                heightAttr = imageSize.height.toString()
              }

              var width = widthAttr.toInt().dp
              var height = heightAttr.toInt().dp

              if (width.value > maxImageWidth) {
                width = maxImageWidth.dp
                height *= width.value / maxImageWidth
              }

              val inlineContent = InlineTextContent(
                placeholder = Placeholder(
                  width = density.run { width.toSp() },
                  height = density.run { height.toSp() },
                  placeholderVerticalAlign = PlaceholderVerticalAlign.Bottom
                ),
                children = {
                  Image(
                    modifier = Modifier
                      .width(width)
                      .height(height),
                    painter = rememberImagePainter(imageUrl),
                    contentDescription = null
                  )
                }
              )

              commentElements.add(CommentInlineContent(
                id = (++incrementIdForInlineContent).toString(),
                content = inlineContent
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

private class ImageSize(
  val width: Int = 0,
  val height: Int = 0
)

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