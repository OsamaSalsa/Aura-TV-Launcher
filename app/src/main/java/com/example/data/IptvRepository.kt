package com.example.data

import com.example.data.local.IptvDao
import com.example.data.local.LauncherPreferences
import com.example.data.model.IptvChannel
import com.example.data.model.IptvPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class IptvRepository(
    private val iptvDao: IptvDao,
    private val preferences: LauncherPreferences
) {
    val activePlaylistFlow: Flow<IptvPlaylist?> = iptvDao.getActivePlaylist()
    val playlistsFlow: Flow<List<IptvPlaylist>> = iptvDao.getAllPlaylists()

    // Unified and deduplicated Favorite Channels Flow across ALL playlists!
    val favoriteChannelsFlow: Flow<List<IptvChannel>> = iptvDao.getFavoriteChannels()
        .map { channels ->
            channels.distinctBy { channel ->
                if (channel.streamUrl.isNotBlank()) channel.streamUrl else channel.name
            }
        }

    val categoriesFlow: Flow<List<String>> = activePlaylistFlow.flatMapLatest { active ->
        if (active != null) {
            iptvDao.getCategoriesByPlaylist(active.id)
        } else {
            flowOf(emptyList())
        }
    }

    fun getChannelsByCategory(category: String): Flow<List<IptvChannel>> {
        return activePlaylistFlow.flatMapLatest { active ->
            when (category) {
                // Unified deduplicated favorites across ALL playlists into a single list
                "Favorites", "المفضلة" -> favoriteChannelsFlow
                "All", "" -> if (active != null) iptvDao.getChannelsByPlaylist(active.id) else flowOf(emptyList())
                else -> if (active != null) iptvDao.getChannelsByCategoryAndPlaylist(category, active.id) else flowOf(emptyList())
            }
        }
    }

    fun searchChannels(query: String): Flow<List<IptvChannel>> {
        return activePlaylistFlow.flatMapLatest { active ->
            if (active != null) {
                iptvDao.searchChannelsByPlaylist(query, active.id)
            } else {
                flowOf(emptyList())
            }
        }
    }

    companion object {
        val PRESET_PLAYLISTS = listOf(
            IptvPlaylist(name = "News", url = "https://www.free-codecs.com/tv/news.m3u"),
            IptvPlaylist(name = "Sports", url = "https://www.free-codecs.com/tv/sports.m3u"),
            IptvPlaylist(name = "Movies", url = "https://www.free-codecs.com/tv/movies.m3u"),
            IptvPlaylist(name = "Entertainment", url = "https://www.free-codecs.com/tv/entertainment.m3u"),
            IptvPlaylist(name = "Egypt", url = "https://www.free-codecs.com/tv/eg.m3u"),
            IptvPlaylist(name = "Morocco", url = "https://www.free-codecs.com/tv/ma.m3u"),
            IptvPlaylist(name = "Music", url = "https://www.free-codecs.com/tv/music.m3u"),
            IptvPlaylist(name = "Kids", url = "https://www.free-codecs.com/tv/kids.m3u"),
            IptvPlaylist(name = "Italy", url = "https://www.free-codecs.com/tv/it.m3u"),
            IptvPlaylist(name = "Ukraine", url = "https://www.free-codecs.com/tv/ua.m3u"),
            IptvPlaylist(name = "Russia", url = "https://www.free-codecs.com/tv/ru.m3u"),
            IptvPlaylist(name = "Israel", url = "https://www.free-codecs.com/tv/il.m3u"),
            IptvPlaylist(name = "Full", url = "https://iptv-org.github.io/iptv/index.language.m3u")
        )
    }

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        loadPresetPlaylists()

        val active = iptvDao.getActivePlaylist().first()
        if (active != null && iptvDao.getCountByPlaylist(active.id) == 0) {
            refreshPlaylistChannels(active.id, active.url)
        } else if (active == null) {
            val firstPlaylist = iptvDao.getAllPlaylists().first().firstOrNull()
            if (firstPlaylist != null) {
                switchActivePlaylist(firstPlaylist.id)
            }
        }
    }

    suspend fun loadPresetPlaylists(): Result<Int> = withContext(Dispatchers.IO) {
        val existingPlaylists = iptvDao.getAllPlaylists().first()
        val existingUrls = existingPlaylists.map { it.url }.toSet()

        var addedCount = 0
        var activePlaylistToParse: IptvPlaylist? = null

        PRESET_PLAYLISTS.forEach { preset ->
            if (!existingUrls.contains(preset.url)) {
                val isFirst = iptvDao.getPlaylistCount() == 0
                val shouldActivate = isFirst && addedCount == 0
                val playlist = preset.copy(isActive = shouldActivate)
                val id = iptvDao.insertPlaylist(playlist)
                if (shouldActivate) {
                    activePlaylistToParse = playlist.copy(id = id)
                }
                addedCount++
            }
        }

        activePlaylistToParse?.let { active ->
            refreshPlaylistChannels(active.id, active.url)
        }

        Result.success(addedCount)
    }

    suspend fun addPlaylist(name: String, url: String, activateImmediately: Boolean = true): Result<Int> = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            return@withContext Result.failure(Exception("Playlist URL cannot be empty"))
        }

        val playlistName = if (name.isBlank()) "Playlist #${iptvDao.getPlaylistCount() + 1}" else name.trim()
        val isFirst = iptvDao.getPlaylistCount() == 0
        val shouldActivate = activateImmediately || isFirst

        val newPlaylist = IptvPlaylist(
            name = playlistName,
            url = url.trim(),
            isActive = shouldActivate
        )
        val playlistId = iptvDao.insertPlaylist(newPlaylist)

        if (shouldActivate) {
            switchActivePlaylist(playlistId)
        } else {
            Result.success(0)
        }
    }

    suspend fun switchActivePlaylist(playlistId: Long): Result<Int> = withContext(Dispatchers.IO) {
        val playlist = iptvDao.getPlaylistById(playlistId)
            ?: return@withContext Result.failure(Exception("Playlist not found"))

        iptvDao.setActivePlaylistInternal(playlistId)

        // Parse and reload channels for this active playlist if not cached yet
        if (iptvDao.getCountByPlaylist(playlistId) == 0) {
            refreshPlaylistChannels(playlistId, playlist.url)
        } else {
            Result.success(iptvDao.getCountByPlaylist(playlistId))
        }
    }

    suspend fun refreshActivePlaylist(): Result<Int> = withContext(Dispatchers.IO) {
        val active = iptvDao.getActivePlaylist().first()
            ?: return@withContext Result.failure(Exception("No active playlist selected"))
        refreshPlaylistChannels(active.id, active.url)
    }

    suspend fun deletePlaylist(playlistId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val active = iptvDao.getActivePlaylist().first()
        val wasActive = active?.id == playlistId

        iptvDao.deletePlaylist(playlistId)
        iptvDao.deleteChannelsForPlaylist(playlistId)

        val remainingPlaylists = iptvDao.getAllPlaylists().first()
        if (wasActive && remainingPlaylists.isNotEmpty()) {
            val nextActive = remainingPlaylists.first()
            switchActivePlaylist(nextActive.id)
        }
        Result.success(Unit)
    }

    private suspend fun refreshPlaylistChannels(playlistId: Long, url: String): Result<Int> {
        return try {
            val parsedChannels = IptvParser.parseFromUrl(url)
            if (parsedChannels.isNotEmpty()) {
                // Keep favorite streams across reloads
                val existingFavUrls = iptvDao.getFavoriteChannels().first().map { it.streamUrl }.toSet()

                iptvDao.clearNonFavoritesByPlaylist(playlistId)

                val channelsToInsert = parsedChannels.map { channel ->
                    channel.copy(
                        playlistId = playlistId,
                        isFavorite = existingFavUrls.contains(channel.streamUrl)
                    )
                }
                iptvDao.insertAll(channelsToInsert)

                val existingPlaylist = iptvDao.getPlaylistById(playlistId)
                if (existingPlaylist != null) {
                    iptvDao.updatePlaylist(existingPlaylist.copy(channelCount = parsedChannels.size, isActive = true))
                }
                Result.success(parsedChannels.size)
            } else {
                Result.failure(Exception("Playlist contains no valid stream channels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(channel: IptvChannel) = withContext(Dispatchers.IO) {
        val newStatus = !channel.isFavorite
        if (channel.streamUrl.isNotBlank()) {
            iptvDao.setFavoriteByStreamUrl(channel.id, channel.streamUrl, newStatus)
        } else {
            iptvDao.setFavorite(channel.id, newStatus)
        }
    }

    suspend fun saveFavoriteOrder(channels: List<IptvChannel>) = withContext(Dispatchers.IO) {
        channels.forEachIndexed { index, channel ->
            iptvDao.updateOrderIndex(channel.id, index)
        }
    }
}
