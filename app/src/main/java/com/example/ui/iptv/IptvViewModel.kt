package com.example.ui.iptv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.IptvRepository
import com.example.data.model.IptvChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.IptvPlaylist

@OptIn(ExperimentalCoroutinesApi::class)
class IptvViewModel(
    private val iptvRepository: IptvRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val playlists: StateFlow<List<IptvPlaylist>> = iptvRepository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlaylist: StateFlow<IptvPlaylist?> = iptvRepository.activePlaylistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rawCategories: StateFlow<List<String>> = iptvRepository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _selectedCategory.value = "All"
            _searchQuery.value = ""
            iptvRepository.switchActivePlaylist(playlistId)
        }
    }

    val channels: StateFlow<List<IptvChannel>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        category to query
    }.flatMapLatest { (category, query) ->
        if (query.isNotBlank()) {
            iptvRepository.searchChannels(query)
        } else {
            iptvRepository.getChannelsByCategory(category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentPlayingChannel = MutableStateFlow<IptvChannel?>(null)
    val currentPlayingChannel: StateFlow<IptvChannel?> = _currentPlayingChannel.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun playChannel(channel: IptvChannel) {
        _playbackError.value = null
        _currentPlayingChannel.value = channel
    }

    fun closePlayer() {
        _currentPlayingChannel.value = null
        _playbackError.value = null
    }

    fun nextChannel() {
        val currentList = channels.value
        val current = _currentPlayingChannel.value ?: return
        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentList.isNotEmpty()) {
            val nextIndex = (currentIndex + 1) % currentList.size
            playChannel(currentList[nextIndex])
        }
    }

    fun prevChannel() {
        val currentList = channels.value
        val current = _currentPlayingChannel.value ?: return
        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentList.isNotEmpty()) {
            val prevIndex = if (currentIndex - 1 < 0) currentList.lastIndex else currentIndex - 1
            playChannel(currentList[prevIndex])
        }
    }

    fun toggleFavorite(channel: IptvChannel) {
        viewModelScope.launch {
            iptvRepository.toggleFavorite(channel)
        }
    }

    fun setPlaybackError(error: String?) {
        _playbackError.value = error
    }

    fun retryPlayback() {
        val current = _currentPlayingChannel.value
        if (current != null) {
            _playbackError.value = null
            // Trigger restart by briefly setting to null or resetting
            val target = current.copy()
            _currentPlayingChannel.value = null
            _currentPlayingChannel.value = target
        }
    }
}
