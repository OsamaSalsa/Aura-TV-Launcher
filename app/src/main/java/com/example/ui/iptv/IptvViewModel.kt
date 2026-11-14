package com.example.ui.iptv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChannelEpgInfo
import com.example.data.EpgManager
import com.example.data.EpgProgram
import com.example.data.IptvRepository
import com.example.data.local.LauncherPreferences
import com.example.data.model.IptvChannel
import com.example.data.model.IptvPlaylist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IptvViewModel(
    private val iptvRepository: IptvRepository,
    private val preferences: LauncherPreferences
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

    // Category Lock & PIN Protection
    val lockedCategories: StateFlow<Set<String>> = preferences.lockedIptvCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val appLockPin: StateFlow<String> = preferences.appLockPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0000")

    private val _sessionUnlockedCategories = MutableStateFlow<Set<String>>(emptySet())
    val sessionUnlockedCategories: StateFlow<Set<String>> = _sessionUnlockedCategories.asStateFlow()

    // Resume Last Played Channel
    val lastPlayedChannelId: StateFlow<Long> = preferences.lastPlayedChannelIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1L)

    val autoResumeLastChannel: StateFlow<Boolean> = preferences.autoResumeLastChannelFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // EPG Guide State
    val epgUrlOverride: StateFlow<String> = preferences.epgUrlOverrideFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val epgState: StateFlow<Map<String, List<EpgProgram>>> = EpgManager.epgState
    val epgIsLoading: StateFlow<Boolean> = EpgManager.isLoading

    init {
        viewModelScope.launch {
            combine(activePlaylist, epgUrlOverride) { playlist, override ->
                override.ifBlank { playlist?.epgUrl.orEmpty() }
            }.collectLatest { url ->
                if (url.isNotBlank()) {
                    EpgManager.loadEpgIfNeeded(url)
                }
            }
        }
    }

    val lastPlayedChannel: StateFlow<IptvChannel?> = combine(
        iptvRepository.getChannelsByCategory("All"),
        lastPlayedChannelId,
        lockedCategories,
        _sessionUnlockedCategories
    ) { allChannels, lastId, locked, unlocked ->
        if (lastId <= 0L) return@combine null
        val ch = allChannels.firstOrNull { it.id == lastId } ?: return@combine null
        val blocked = locked.contains(ch.groupTitle) && !unlocked.contains(ch.groupTitle)
        if (blocked) null else ch
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
            val effectiveEpg = epgUrlOverride.value.ifBlank { activePlaylist.value?.epgUrl.orEmpty() }
            if (effectiveEpg.isNotBlank()) {
                EpgManager.loadEpgIfNeeded(effectiveEpg, force = true)
            }
            _isSyncing.value = false
        }
    }

    fun refreshEpg(force: Boolean = true) {
        viewModelScope.launch {
            val effectiveEpg = epgUrlOverride.value.ifBlank { activePlaylist.value?.epgUrl.orEmpty() }
            if (effectiveEpg.isNotBlank()) {
                EpgManager.loadEpgIfNeeded(effectiveEpg, force = force)
            }
        }
    }

    fun setEpgUrlOverride(url: String) {
        viewModelScope.launch {
            val trimmed = url.trim()
            preferences.setEpgUrlOverride(trimmed)
            val effectiveEpg = trimmed.ifBlank { activePlaylist.value?.epgUrl.orEmpty() }
            if (effectiveEpg.isNotBlank()) {
                EpgManager.loadEpgIfNeeded(effectiveEpg, force = true)
            }
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
        _searchQuery,
        lockedCategories,
        _sessionUnlockedCategories
    ) { category, query, locked, unlocked ->
        QuadChannelQuery(category, query, locked, unlocked)
    }.flatMapLatest { q ->
        val rawFlow = if (q.query.isNotBlank()) {
            iptvRepository.searchChannels(q.query)
        } else {
            iptvRepository.getChannelsByCategory(q.category)
        }
        combine(rawFlow, MutableStateFlow(q)) { list, queryInfo ->
            val hiddenLocked = queryInfo.locked - queryInfo.unlocked
            if (hiddenLocked.isEmpty()) {
                list
            } else {
                list.filter { !hiddenLocked.contains(it.groupTitle) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class QuadChannelQuery(
        val category: String,
        val query: String,
        val locked: Set<String>,
        val unlocked: Set<String>
    )

    private val _currentPlayingChannel = MutableStateFlow<IptvChannel?>(null)
    val currentPlayingChannel: StateFlow<IptvChannel?> = _currentPlayingChannel.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    fun unlockCategory(category: String) {
        _sessionUnlockedCategories.value = _sessionUnlockedCategories.value + category
    }

    fun toggleCategoryLock(category: String) {
        if (category == "All" || category == "Favorites") return
        viewModelScope.launch {
            val isCurrentlyLocked = lockedCategories.value.contains(category)
            val willLock = !isCurrentlyLocked
            preferences.setIptvCategoryLocked(category, willLock)
            if (willLock) {
                _sessionUnlockedCategories.value = _sessionUnlockedCategories.value - category
                if (_selectedCategory.value == category) {
                    _selectedCategory.value = "All"
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun playChannel(channel: IptvChannel) {
        _playbackError.value = null
        _currentPlayingChannel.value = channel
        viewModelScope.launch {
            preferences.setLastPlayedChannelId(channel.id)
        }
    }

    fun resumeLastChannel() {
        lastPlayedChannel.value?.let { playChannel(it) }
    }

    fun getChannelEpg(channel: IptvChannel, programsMap: Map<String, List<EpgProgram>>): ChannelEpgInfo {
        return EpgManager.getChannelNowNext(channel, programsById = programsMap)
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
