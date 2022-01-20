package com.moegirlviewer.screen.captcha

import com.moegirlviewer.util.RouteArguments
import com.moegirlviewer.util.RouteName

@RouteName("captcha")
class CaptchaRouteArguments (
  val captchaHtml: String,
) : RouteArguments() {
  constructor() : this("")
}