package com.videorecodercompose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.videorecodercompose.ui.theme.VideoRecoderComposeTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoRecoderComposeTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                )
                }
                // A surface container using the 'background' color from the theme

                /*    NavHost(
                        navController = navController,
                        startDestination = Route.VIDEO
                    ) {
                        composable(Route.VIDEO) {
                            VideoCaptureScreen(navController = navController)
                        }

                       *//* composable(Route.VIDEO_PREVIEW_FULL_ROUTE) {
                            val uri = it.arguments?.getString(VIDEO_PREVIEW_ARG) ?: ""
                            VideoPreviewScreen(uri = uri)
                        }*//*
                    }*/
            }
        }

    }



object Route {
    const val VIDEO = "video"
    const val MAIN = "main"
    const val VIDEO_PREVIEW_FULL_ROUTE = "video_preview/{uri}"
    const val VIDEO_PREVIEW = "video_preview"
    const val VIDEO_PREVIEW_ARG = "uri"
}