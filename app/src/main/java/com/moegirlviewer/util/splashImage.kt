package com.moegirlviewer.util

import com.moegirlviewer.R

open class SplashImage(
  val resId: Int,
  val title: String,
  val author: String,
  val season: String
)

class SplashImageWithKey(
  splashImage: SplashImage,
  val key: SplashImageKey
) : SplashImage(
  resId = splashImage.resId,
  title = splashImage.title,
  author = splashImage.author,
  season = splashImage.season
)

enum class SplashImageKey {
  _2015_4,

  _2016_1,
  _2016_2,
  _2016_3,
  _2016_4,

  _2017_1,
  _2017_2,
  _2017_3,
  _2017_4,

  _2018_1,
  _2018_2,
  _2018_3,
  _2018_4,

  _2019_1,
  _2019_2,
  _2019_3,
  _2019_4,

  _2020_1,
  _2020_3,
  _2020_3_10TH_ANNIVERSARY,

  _2021_2,
  _2021_3,

  _2022_1;

  companion object {
    fun getSplashImages(): List<SplashImageWithKey> {
      return values().map {
        SplashImageWithKey(it.toSplashImage(), it)
      }
    }

    fun SplashImageKey.toSplashImage() = when(this) {
      _2015_4 -> SplashImage(
        resId = R.mipmap.splash_2015_4,
        title = Globals.context.getString(R.string.splashImageTitle_2015_4),
        author = "Bison仓鼠",
        season = "2015年冬"
      )
      _2016_1 -> SplashImage(
        resId = R.mipmap.splash_2016_1,
        title = Globals.context.getString(R.string.splashImageTitle_2016_1),
        author = "M.vv",
        season = "2016年春"
      )
      _2016_2 -> SplashImage(
        resId = R.mipmap.splash_2016_2,
        title = Globals.context.getString(R.string.splashImageTitle_2016_2),
        author = "Bison仓鼠",
        season = "2016年夏"
      )
      _2016_3 -> SplashImage(
        resId = R.mipmap.splash_2016_3,
        title = Globals.context.getString(R.string.splashImageTitle_2016_3),
        author = "BIBIA",
        season = "2016年秋"
      )
      _2016_4 -> SplashImage(
        resId = R.mipmap.splash_2016_4,
        title = Globals.context.getString(R.string.splashImageTitle_2016_4),
        author = "稀泥m",
        season = "2016年冬"
      )
      _2017_1 -> SplashImage(
        resId = R.mipmap.splash_2017_1,
        title = Globals.context.getString(R.string.splashImageTitle_2017_1),
        author = "傲娇团子",
        season = "2017年春"
      )
      _2017_2 -> SplashImage(
        resId = R.mipmap.splash_2017_2,
        title = Globals.context.getString(R.string.splashImageTitle_2017_2),
        author = "夏风（shuffle）",
        season = "2017年夏"
      )
      _2017_3 -> SplashImage(
        resId = R.mipmap.splash_2017_3,
        title = Globals.context.getString(R.string.splashImageTitle_2017_3),
        author = "白祈QSR",
        season = "2017年秋"
      )
      _2017_4 -> SplashImage(
        resId = R.mipmap.splash_2017_4,
        title = Globals.context.getString(R.string.splashImageTitle_2017_4),
        author = "巫贼",
        season = "2017年冬"
      )
      _2018_1 -> SplashImage(
        resId = R.mipmap.splash_2018_1,
        title = Globals.context.getString(R.string.splashImageTitle_2018_1),
        author = "咩煲",
        season = "2018年春"
      )
      _2018_2 -> SplashImage(
        resId = R.mipmap.splash_2018_2,
        title = Globals.context.getString(R.string.splashImageTitle_2018_2),
        author = "飞翔的秀吉",
        season = "2018年夏"
      )
      _2018_3 -> SplashImage(
        resId = R.mipmap.splash_2018_3,
        title = Globals.context.getString(R.string.splashImageTitle_2018_3),
        author = "Phi",
        season = "2018年秋"
      )
      _2018_4 -> SplashImage(
        resId = R.mipmap.splash_2018_4,
        title = Globals.context.getString(R.string.splashImageTitle_2018_4),
        author = "贼小米球",
        season = "2018年冬"
      )
      _2019_1 -> SplashImage(
        resId = R.mipmap.splash_2019_1,
        title = Globals.context.getString(R.string.splashImageTitle_2019_1),
        author = "哈奈鲁",
        season = "2019年春"
      )
      _2019_2 -> SplashImage(
        resId = R.mipmap.splash_2019_2,
        title = Globals.context.getString(R.string.splashImageTitle_2019_2),
        author = "星空下的拥抱",
        season = "2019年夏"
      )
      _2019_3 -> SplashImage(
        resId = R.mipmap.splash_2019_3,
        title = Globals.context.getString(R.string.splashImageTitle_2019_3),
        author = "和茶",
        season = "2019年秋"
      )
      _2019_4 -> SplashImage(
        resId = R.mipmap.splash_2019_4,
        title = Globals.context.getString(R.string.splashImageTitle_2019_4),
        author = "八云油豆腐",
        season = "2019年冬"
      )
      _2020_1 -> SplashImage(
        resId = R.mipmap.splash_2020_1,
        title = Globals.context.getString(R.string.splashImageTitle_2020_1),
        author = "Lnike",
        season = "2020年春"
      )
      _2020_3 -> SplashImage(
        resId = R.mipmap.splash_2020_3,
        title = Globals.context.getString(R.string.splashImageTitle_2020_3),
        author = "萌子芽",
        season = "2020年秋"
      )
      _2020_3_10TH_ANNIVERSARY -> SplashImage(
        resId = R.mipmap.splash_2020_3_10th_anniversary,
        title = Globals.context.getString(R.string.splashImageTitle_2020_3_10th_anniversary),
        author = "山桂贰",
        season = "2020年秋"
      )
      _2021_2 -> SplashImage(
        resId = R.mipmap.splash_2021_2,
        title = Globals.context.getString(R.string.splashImageTitle_2021_2),
        author = "萌派·rika",
        season = "2021年夏"
      )
      _2021_3 -> SplashImage(
        resId = R.mipmap.splash_2021_3,
        title = Globals.context.getString(R.string.splashImageTitle_2021_3),
        author = "千夨chia",
        season = "2021年秋"
      )
      _2022_1 -> SplashImage(
        resId = R.mipmap.splash_2022_1,
        title = Globals.context.getString(R.string.splashImageTitle_2022_1),
        author = "千夨chia",
        season = "2022年春"
      )
    }
  }
}