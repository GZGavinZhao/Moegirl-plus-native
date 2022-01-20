package chino.kawaii.dmplayer.page.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import chino.kawaii.dmplayer.ui.theme.DmplayerTheme

class ${NAME} : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            layout()
        }
    }
}

@Composable
fun layout() {
    DmplayerTheme {
        Text(text = "${NAME}")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    layout()
}