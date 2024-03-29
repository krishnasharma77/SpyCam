package com.videorecodercompose


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun NavGraph(
    navController: NavHostController,
) {


    NavHost(
        navController = navController,
        startDestination = Route.MAIN
    ) {
            composable(Route.MAIN) {
                MainScreen(navController = navController)
            }
        composable(Route.VIDEO) {
                VideoCaptureScreen(navController = navController)
            }
    }
}