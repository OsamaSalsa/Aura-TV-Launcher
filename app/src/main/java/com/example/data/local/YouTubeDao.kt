package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.YouTubeChannel
import kotlinx.coroutines.flow.Flow

@Dao
interface YouTubeDao {
    @Query("SELECT * FROM youtube_allowed_channels ORDER BY name ASC")
    fun getAllowedChannels(): Flow<List<YouTubeChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: YouTubeChannel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<YouTubeChannel>)

    @Query("DELETE FROM youtube_allowed_channels WHERE channelId = :channelId")
    suspend fun deleteChannel(channelId: String)

    @Query("SELECT COUNT(*) FROM youtube_allowed_channels")
    suspend fun getCount(): Int

    @Query("DELETE FROM youtube_allowed_channels")
    suspend fun clearAll()
}
