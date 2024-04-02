package com.videorecodercompose.screens

import android.Manifest
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ContentValues
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.videorecodercompose.R
import com.videorecodercompose.utils.createVideoCaptureUseCase
import com.videorecodercompose.utils.startRecordingVideo
import kotlinx.coroutines.launch
import java.io.File

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCaptureScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    var recording: Recording? = remember { null }
    val previewView: PreviewView = remember { PreviewView(context) }
    val videoCapture: MutableState<VideoCapture<Recorder>?> = remember { mutableStateOf(null) }
    val recordingStarted: MutableState<Boolean> = remember { mutableStateOf(false) }

    val audioEnabled: MutableState<Boolean> = remember { mutableStateOf(false) }
    val cameraSelector: MutableState<CameraSelector> = remember {
        mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(previewView) {
        videoCapture.value = context.createVideoCaptureUseCase(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector.value,
            previewView = previewView
        )
    }

    LaunchedEffect(recordingStarted.value) {
        if (recordingStarted.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("VideoCaptureScreen", "Entering PiP mode")
            val aspectRatio = Rational(1, 1) // Aspect ratio for PiP mode
            val screenSize = Size(1920, 1080) // Screen size for PiP mode
            val sourceRect = Rect(0, 0, screenSize.width, screenSize.height) // Create Rect object

            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(sourceRect) // Use Rect object for sourceRectHint
                .build()

            (context as Activity).enterPictureInPictureMode(params)
        }
    }

    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = { /* ... */ },
        permissionsNotAvailableContent = { /* ... */ }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .height(0.dp)
                    .width(0.dp)
                    .align(Alignment.Center)
            )
            IconButton(
                onClick = {
                    if (!recordingStarted.value) {
                        videoCapture.value?.let { videoCapture ->
                            recordingStarted.value = true
                            val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
                                File(it, context.getString(R.string.app_name)).apply { mkdirs() }
                            }
                            recording = startRecordingVideo(
                                context = context,
                                filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                                videoCapture = videoCapture,
                                outputDirectory = if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir,
                                executor = context.mainExecutor,
                                audioEnabled = audioEnabled.value
                            ) { event ->
                                if (event is VideoRecordEvent.Finalize) {
                                    val uri = event.outputResults.outputUri
                                    if (uri != Uri.EMPTY) {
                                        saveVideoToGallery(context, uri)
                                    }
                                }
                            }
                        }
                    } else {
                        recordingStarted.value = false
                        recording?.stop()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    painter = painterResource(if (recordingStarted.value) R.drawable.ic_stop else R.drawable.ic_record),
                    contentDescription = "",
                    modifier = Modifier.size(64.dp)
                )
            }
            if (!recordingStarted.value) {
                IconButton(
                    onClick = {
                        audioEnabled.value = !audioEnabled.value
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 32.dp)
                ) {
                    Icon(
                        painter = painterResource(if (audioEnabled.value) R.drawable.ic_mic_on else R.drawable.ic_mic_off),
                        contentDescription = "",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            if (!recordingStarted.value) {
                IconButton(
                    onClick = {
                        cameraSelector.value =
                            if (cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                            else CameraSelector.DEFAULT_BACK_CAMERA
                        lifecycleOwner.lifecycleScope.launch {
                            videoCapture.value = context.createVideoCaptureUseCase(
                                lifecycleOwner = lifecycleOwner,
                                cameraSelector = cameraSelector.value,
                                previewView = previewView
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_switch_camera),
                        contentDescription = "",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

    }
}

fun saveVideoToGallery(context: Context, videoUri: Uri) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "RecordedVideo")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
    }
    val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val uri = resolver.insert(contentUri, contentValues)

    uri?.let { destinationUri ->
        resolver.openOutputStream(destinationUri)?.use { outputStream ->
            resolver.openInputStream(videoUri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
                Log.d("VideoRecorder", "Video saved to gallery: $destinationUri")
            }
        }
    }
}
