package com.moegirlviewer.screen.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HotelClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.moegirlviewer.R
import com.moegirlviewer.api.page.PageApi
import com.moegirlviewer.compable.remember.rememberImageRequest
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.request.MoeRequestException
import com.moegirlviewer.screen.home.HomeScreenCardState
import com.moegirlviewer.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CarouseCard(
  state: CarouseCardState,
) {
  val themeColors = MaterialTheme.colors
  val scope = rememberCoroutineScope()

  LaunchedEffect(true) {
    suspend fun loop() {
      delay(3000)
      if (state.status == LoadStatus.SUCCESS) {
        var nextPage = state.pagerState.currentPage + 1
        if (nextPage == state.pagerState.pageCount) nextPage = 0
        state.pagerState.animateScrollToPage(nextPage)
      }

      loop()
    }

    loop()
  }

  HomeCard(
    modifier = Modifier
      .padding(15.dp),
    loadStatus = state.status,
    minHeight = 250.dp,
    onReload = {
      scope.launch { state.reload() }
    }
  ) {
    RippleColorScope(color = themeColors.primaryVariant) {
      HorizontalPager(
        count = state.imageList.size,
        state = state.pagerState,
        userScrollEnabled = false
      ) { currentPage ->
        val item = state.imageList[currentPage]

        Box(
          modifier = Modifier
//              .padding(15.dp)
            .fillMaxSize()
            .height(250.dp)
            .clickable { gotoArticlePage(item.pageName) },
          contentAlignment = Alignment.BottomCenter
        ) {
          AsyncImage(
            modifier = Modifier
              .fillMaxSize(),
            model = rememberImageRequest(item.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(42.dp)
              .background(
                brush = Brush.verticalGradient(listOf(
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.7f)
                ))
              )
              .blur(5.dp),
            contentAlignment = Alignment.Center
          ) {
            StyledText(
              text = item.intro,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              fontSize = 16.sp,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalPagerApi::class)
class CarouseCardState : HomeScreenCardState() {
  var status by mutableStateOf(LoadStatus.INITIAL)
  var imageList by mutableStateOf(emptyList<CarouseImage>())
  var pagerState = PagerState(0)

  override suspend fun reload() {
    status = LoadStatus.LOADING
    try {
      val res = PageApi.getPageContent(PageNameKey("Template:首页/典范条目"), previewMode = true)
      val htmlContent = res.parse.text._asterisk
      val htmlDoc = Jsoup.parse(htmlContent)

      imageList = htmlDoc.getElementsByClass("mainpage-banner-page")
        .map {
          val pageName = it.getElementsByTag("a").first()!!.attr("title")
          val intro = it.getElementsByClass("mainpage-page-intro").first()!!.text()
          val imageUrl = it.getElementsByClass("mainpage-page-img").first()!!.attr("src")
          CarouseImage(
            imageUrl = imageUrl,
            intro = intro,
            pageName = pageName
          )
        }
      status = LoadStatus.SUCCESS
    } catch (e: MoeRequestException) {
      printRequestErr(e, "获取H萌娘轮播图片失败")
      status = LoadStatus.FAIL
    }
  }
}

class CarouseImage(
  val imageUrl: String,
  val intro: String,
  val pageName: String
)