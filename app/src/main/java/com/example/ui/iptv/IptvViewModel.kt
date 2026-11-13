package com.example.ui.iptv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.IptvRepository
import com.example.data.model.IptvChannel
import com.example.data.model.IptvPlaylist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IptvViewModel(
    private val iptvRepository: IptvRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val playlists: StateFlow<List<IptvPlaylist>> = iptvRepository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlaylist: StateFlow<IptvPlaylist?> = iptvRepository.activePlaylistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rawCategories: StateFlow<List<String>> = iptvRepository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<IptvChannel>> = iptvRepository.favoriteChannelsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _isSyncing.value = true
            _selectedCategory.value = "All"
            _searchQuery.value = ""
            iptvRepository.switchActivePlaylist(playlistId)
            _isSyncing.value = false
        }
    }

    fun refreshActivePlaylist() {
        viewModelScope.launch {
            _isSyncing.value = true
            iptvRepository.refreshActivePlaylist()
            _isSyncing.value = false
        }
    }

    fun addPlaylist(name: String, url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _isSyncing.value = true
            iptvRepository.addPlaylist(name, url, activateImmediately = true)
            _isSyncing.value = false
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            iptvRepository.deletePlaylist(playlistId)
        }
    }

    fun deleteAllPlaylists() {
        viewModelScope.launch {
            iptvRepository.deleteAllPlaylists()
        }
    }

    fun loadPresetPlaylists() {
        viewModelScope.launch {
            _isSyncing.value = true
            iptvRepository.loadPresetPlaylists()
            _isSyncing.value = false
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
            val current = _currentPlayingChannel.value
            if (current != null && current.id == channel.id) {
                _currentPlayingChannel.value = current.copy(isFavorite = !current.isFavorite)
            }
        }
    }

    fun setPlaybackError(error: String?) {
        _playbackError.value = error
    }

    fun retryPlayback() {
        val current = _currentPlayingChannel.value
        if (current != null) {
            _playbackError.value = null
            val target = current.copy()
            _currentPlayingChannel.value = null
            _currentPlayingChannel.value = target
        }
    }
}
