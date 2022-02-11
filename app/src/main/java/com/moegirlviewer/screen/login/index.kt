package com.moegirlviewer.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moegirlviewer.Constants
import com.moegirlviewer.R
import com.moegirlviewer.component.RippleColorScope
import com.moegirlviewer.component.styled.StyledText
import com.moegirlviewer.screen.login.component.LoginScreenTextField
import com.moegirlviewer.util.imeBottomPadding
import com.moegirlviewer.util.isMoegirl
import com.moegirlviewer.util.noRippleClickable
import com.moegirlviewer.util.openHttpUrl
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
  val model: LoginScreenModel = hiltViewModel()
  val scope = rememberCoroutineScope()

  Scaffold() {
    RippleColorScope(Color.White) {
      Box(
        modifier = Modifier
          .imeBottomPadding()
          .fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          modifier = Modifier
            .fillMaxSize(),
          painter = painterResource(R.mipmap.splash_2017_1),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          colorFilter = ColorFilter.tint(
            color = Color(0f, 0f, 0f, 0.5f),
            blendMode = BlendMode.Darken
          )
        )

        Column(
          modifier = Modifier
            .width(280.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Image(
            modifier = Modifier
              .width(70.dp)
              .height(70.dp),
            painter = painterResource(R.drawable.moemoji),
            contentDescription = null,
          )

          StyledText(
            modifier = Modifier
              .padding(vertical = 10.dp),
            text = stringResource(R.string.moegirlSloganText),
            fontSize = 16.sp,
            color = if (isMoegirl()) Color(0xFFC8E6C9) else Color(0xFFFFE686)
          )

          LoginScreenTextField(
            modifier = Modifier
              .padding(vertical = 8.dp)
              .fillMaxWidth(),
            value = model.userName,
            label = stringResource(R.string.userName),
            onValueChange = { model.userName = it }
          )

          LoginScreenTextField(
            modifier = Modifier
              .padding(top = 10.dp)
              .fillMaxWidth(),
            value = model.password,
            label = stringResource(R.string.password),
            password = true,
            onValueChange = { model.password = it },
            onAction = { scope.launch { model.submit() } }
          )

          Box(
            modifier = Modifier
              .padding(top = 20.dp)
              .width(280.dp)
              .height(45.dp)
              .clip(shape = RoundedCornerShape(5.dp))
              .background(if (isMoegirl()) Color(0xFF4CAF50) else Color(0xffFFE686))
              .clickable { scope.launch { model.submit() } }
            ,
            contentAlignment = Alignment.Center
          ) {
            StyledText(
              text = stringResource(R.string.login),
              color = isMoegirl(Color.White, Color.Black),
            )
          }

          StyledText(
            modifier = Modifier
              .padding(top = 10.dp)
              .noRippleClickable { openHttpUrl(Constants.registerUrl) },
            text = stringResource(id = R.string.noAccountHint),
            color = Color.White,
            fontSize = 17.sp,
            textDecoration = TextDecoration.Underline
          )
        }
      }
    }
  }
}

