package com.example.ui.youtube

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun YouTubeScreen(
    viewModel: YouTubeViewModel,
    onOpenSettings: () -> Unit,
    onBackToHome: () -> Unit
) {
    val currentTvUrl by viewModel.currentTvUrl.collectAsState()
    val reloadTrigger by viewModel.reloadTrigger.collectAsState()

    BackHandler {
        viewModel.setVideoPlaying(false)
        onBackToHome()
    }

    YouTubeTvWebView(
        url = currentTvUrl,
        reloadTrigger = reloadTrigger,
        onBackToCurated = {
            viewModel.setVideoPlaying(false)
            onBackToHome()
        },
        onVideoPlayingChanged = { playing ->
            viewModel.setVideoPlaying(playing)
        },
        modifier = Modifier.fillMaxSize()
    )
}
