package com.moegirlviewer.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.node.Ref
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

// compose material的slider组件的手势貌似有问题，这里封装一下view组件
@Composable
fun ComposeSlider(
  value: Float,
  valueRange: ClosedRange<Float>,
  stepSize: Float,
  onValueChange: (Float) -> Unit
) {
  val sliderView = remember { Ref<Slider>() }

  LaunchedEffect(value) {
    sliderView.value?.value = value
  }

  AndroidView(
    factory = {
      Slider(it).apply {
        sliderView.value = this
        this.value = value
        this.valueFrom = valueRange.start
        this.valueTo = valueRange.endInclusive
        this.stepSize = stepSize
        this.labelBehavior = LabelFormatter.LABEL_GONE

        this.addOnChangeListener { slider, value, fromUser ->
          onValueChange(value)
        }
      }
    }
  )
}