package com.moegirlviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import com.moegirlviewer.R

open class SplashImage(
  val composePainter: @Composable () -> Painter,
  val title: String,
  val author: String,
)

class MoegirlSplashImage(
  resId: Int,
  title: String,
  author: String,
  val key: String,
  val season: String,
) : SplashImage(
  composePainter = { painterResource(resId) },
  title = title,
  author = author
)

val splashImageList = listOf(
  MoegirlSplashImage(
    key = "_2015_4",
    resId = R.mipmap.splash_2015_4,
    title = Globals.context.getString(R.string.splashImageTitle_2015_4),
    author = "Bison仓鼠",
    season = "2015年冬"
  ),
  MoegirlSplashImage(
    key = "_2016_1",
    resId = R.mipmap.splash_2016_1,
    title = Globals.context.getString(R.string.splashImageTitle_2016_1),
    author = "M.vv",
    season = "2016年春"
  ),
  MoegirlSplashImage(
    key = "_2016_2",
    resId = R.mipmap.splash_2016_2,
    title = Globals.context.getString(R.string.splashImageTitle_2016_2),
    author = "Bison仓鼠",
    season = "2016年夏"
  ),
  MoegirlSplashImage(
    key = "_2016_3",
    resId = R.mipmap.splash_2016_3,
    title = Globals.context.getString(R.string.splashImageTitle_2016_3),
    author = "BIBIA",
    season = "2016年秋"
  ),
  MoegirlSplashImage(
    key = "_2016_4",
    resId = R.mipmap.splash_2016_4,
    title = Globals.context.getString(R.string.splashImageTitle_2016_4),
    author = "稀泥m",
    season = "2016年冬"
  ),
  MoegirlSplashImage(
    key = "_2017_1",
    resId = R.mipmap.splash_2017_1,
    title = Globals.context.getString(R.string.splashImageTitle_2017_1),
    author = "傲娇团子",
    season = "2017年春"
  ),
  MoegirlSplashImage(
    key = "_2017_2",
    resId = R.mipmap.splash_2017_2,
    title = Globals.context.getString(R.string.splashImageTitle_2017_2),
    author = "夏风（shuffle）",
    season = "2017年夏"
  ),
  MoegirlSplashImage(
    key = "_2017_3",
    resId = R.mipmap.splash_2017_3,
    title = Globals.context.getString(R.string.splashImageTitle_2017_3),
    author = "白祈QSR",
    season = "2017年秋"
  ),
  MoegirlSplashImage(
    key = "_2017_4",
    resId = R.mipmap.splash_2017_4,
    title = Globals.context.getString(R.string.splashImageTitle_2017_4),
    author = "巫贼",
    season = "2017年冬"
  ),
  MoegirlSplashImage(
    key = "_2018_1",
    resId = R.mipmap.splash_2018_1,
    title = Globals.context.getString(R.string.splashImageTitle_2018_1),
    author = "咩煲",
    season = "2018年春"
  ),
  MoegirlSplashImage(
    key = "_2018_2",
    resId = R.mipmap.splash_2018_2,
    title = Globals.context.getString(R.string.splashImageTitle_2018_2),
    author = "飞翔的秀吉",
    season = "2018年夏"
  ),
  MoegirlSplashImage(
    key = "_2018_3",
    resId = R.mipmap.splash_2018_3,
    title = Globals.context.getString(R.string.splashImageTitle_2018_3),
    author = "Phi",
    season = "2018年秋"
  ),
  MoegirlSplashImage(
    key = "_2018_4",
    resId = R.mipmap.splash_2018_4,
    title = Globals.context.getString(R.string.splashImageTitle_2018_4),
    author = "贼小米球",
    season = "2018年冬"
  ),
  MoegirlSplashImage(
    key = "_2019_1",
    resId = R.mipmap.splash_2019_1,
    title = Globals.context.getString(R.string.splashImageTitle_2019_1),
    author = "哈奈鲁",
    season = "2019年春"
  ),
  MoegirlSplashImage(
    key = "_2019_2",
    resId = R.mipmap.splash_2019_2,
    title = Globals.context.getString(R.string.splashImageTitle_2019_2),
    author = "星空下的拥抱",
    season = "2019年夏"
  ),
  MoegirlSplashImage(
    key = "_2019_3",
    resId = R.mipmap.splash_2019_3,
    title = Globals.context.getString(R.string.splashImageTitle_2019_3),
    author = "和茶",
    season = "2019年秋"
  ),
  MoegirlSplashImage(
    key = "_2019_4",
    resId = R.mipmap.splash_2019_4,
    title = Globals.context.getString(R.string.splashImageTitle_2019_4),
    author = "八云油豆腐",
    season = "2019年冬"
  ),
  MoegirlSplashImage(
    key = "_2020_1",
    resId = R.mipmap.splash_2020_1,
    title = Globals.context.getString(R.string.splashImageTitle_2020_1),
    author = "Lnike",
    season = "2020年春"
  ),
  MoegirlSplashImage(
    key = "_2020_3",
    resId = R.mipmap.splash_2020_3,
    title = Globals.context.getString(R.string.splashImageTitle_2020_3),
    author = "萌子芽",
    season = "2020年秋"
  ),
  MoegirlSplashImage(
    key = "_2020_3_10TH_ANNIVERSARY",
    resId = R.mipmap.splash_2020_3_10th_anniversary,
    title = Globals.context.getString(R.string.splashImageTitle_2020_3_10th_anniversary),
    author = "山桂贰",
    season = "2020年秋"
  ),
  MoegirlSplashImage(
    key = "_2021_2",
    resId = R.mipmap.splash_2021_2,
    title = Globals.context.getString(R.string.splashImageTitle_2021_2),
    author = "萌派·rika",
    season = "2021年夏"
  ),
  MoegirlSplashImage(
    key = "_2021_3",
    resId = R.mipmap.splash_2021_3,
    title = Globals.context.getString(R.string.splashImageTitle_2021_3),
    author = "千夨chia",
    season = "2021年秋"
  ),
  MoegirlSplashImage(
    key = "_2022_1",
    resId = R.mipmap.splash_2022_1,
    title = Globals.context.getString(R.string.splashImageTitle_2022_1),
    author = "千夨chia",
    season = "2022年春"
  )
)