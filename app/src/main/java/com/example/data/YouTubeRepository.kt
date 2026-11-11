package com.example.data

import com.example.data.local.YouTubeDao
import com.example.data.model.YouTubeChannel
import com.example.data.model.YouTubeVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class YouTubeRepository(
    private val youtubeDao: YouTubeDao
) {
    val allowedChannelsFlow: Flow<List<YouTubeChannel>> = youtubeDao.getAllowedChannels()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val count = youtubeDao.getCount()
        if (count == 0) {
            for (channel in DEFAULT_CHANNELS) {
                val realId = YouTubeRssParser.resolveChannelId(channel.channelId)
                val channelToSave = channel.copy(
                    channelId = if (realId.isNotBlank()) realId else channel.channelId
                )
                youtubeDao.insertChannel(channelToSave)
            }
        } else {
            // Ensure default channel "Solace Tones" is present
            val solaceTones = DEFAULT_CHANNELS.find { it.name == "Solace Tones" }
            if (solaceTones != null) {
                val realId = YouTubeRssParser.resolveChannelId(solaceTones.channelId)
                val finalId = if (realId.isNotBlank()) realId else solaceTones.channelId
                youtubeDao.insertChannel(solaceTones.copy(channelId = finalId))
            }
        }
    }

    suspend fun clearAllChannels() = withContext(Dispatchers.IO) {
        youtubeDao.clearAll()
    }

    suspend fun fetchChannelVideos(channelId: String): Result<List<YouTubeVideo>> = withContext(Dispatchers.IO) {
        try {
            val videos = YouTubeRssParser.fetchChannelVideos(channelId)
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addChannel(name: String, channelIdOrUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resolvedId = YouTubeRssParser.resolveChannelId(channelIdOrUrl)
            if (resolvedId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Invalid Channel URL or ID"))
            }

            val channelName = if (name.isNotBlank()) name else {
                if (channelIdOrUrl.contains("@")) "@${channelIdOrUrl.substringAfter("@")}"
                else "Channel $resolvedId"
            }

            val channel = YouTubeChannel(
                channelId = resolvedId,
                name = channelName,
                handle = if (channelIdOrUrl.contains("@")) "@${channelIdOrUrl.substringAfter("@")}" else "",
                avatarUrl = "",
                customAdded = true
            )
            youtubeDao.insertChannel(channel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeChannel(channelId: String) = withContext(Dispatchers.IO) {
        youtubeDao.deleteChannel(channelId)
    }

    companion object {
        val DEFAULT_CHANNELS = listOf(
            YouTubeChannel(
                channelId = "https://www.youtube.com/@SolaceTones",
                name = "Solace Tones",
                handle = "@SolaceTones",
                avatarUrl = "",
                customAdded = false
            )
        )
    }
}
