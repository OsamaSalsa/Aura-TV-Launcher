package com.example.ui.youtube

import androidx.lifecycle.ViewModel
import com.example.data.local.LauncherPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// YouTube TV URL for Leanback interface
private const val YOUTUBE_TV_URL_VM = "https://www.youtube.com/tv"

class YouTubeViewModel(
    private val preferences: LauncherPreferences? = null
) : ViewModel() {

    // Current URL being played inside YouTube.com/tv
    private val _currentTvUrl = MutableStateFlow(YOUTUBE_TV_URL_VM)
    val currentTvUrl: StateFlow<String> = _currentTvUrl.asStateFlow()

    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    fun setVideoPlaying(playing: Boolean) {
        _isVideoPlaying.value = playing
    }

    fun reloadTv() {
        _reloadTrigger.value = _reloadTrigger.value + 1
    }
}
