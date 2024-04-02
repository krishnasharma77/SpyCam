package com.videorecodercompose.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import android.util.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.videorecodercompose.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        content = {
            it
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
//                        enterPiPMode(coroutineScope, context)
                        navController.navigate(Route.VIDEO) },
                    modifier = Modifier
                        .padding(top = 50.dp)
                        .height(56.dp) // Adjust height as needed
                        .wrapContentWidth()
                ) {
                    Text(text = "Recording")
                }
            }
        })
}
/*fun enterPiPMode(coroutineScope: CoroutineScope, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val aspectRatio = Rational(1, 1) // Aspect ratio for PiP mode
        val screenSize = Size(1920, 1080) // Screen size for PiP mode
        val sourceRect = Rect(0, 0, screenSize.width, screenSize.height) // Create Rect object

        val params = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .setSourceRectHint(sourceRect) // Use Rect object for sourceRectHint
            .build()

        coroutineScope.launch {
            (context as Activity).enterPictureInPictureMode(params)
        }
    }
}*/
