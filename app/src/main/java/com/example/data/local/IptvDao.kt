package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.IptvChannel
import com.example.data.model.IptvPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface IptvDao {
    @Query("SELECT * FROM iptv_channels WHERE playlistId = :playlistId ORDER BY orderIndex ASC, name ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<IptvChannel>>

    @Query("SELECT * FROM iptv_channels ORDER BY orderIndex ASC, name ASC")
    fun getAllChannels(): Flow<List<IptvChannel>>

    @Query("SELECT * FROM iptv_channels WHERE isFavorite = 1 ORDER BY orderIndex ASC, name ASC")
    fun getFavoriteChannels(): Flow<List<IptvChannel>>
    
    @Query("UPDATE iptv_channels SET orderIndex = :idx WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, idx: Int)

    @Query("SELECT DISTINCT groupTitle FROM iptv_channels WHERE playlistId = :playlistId ORDER BY groupTitle ASC")
    fun getCategoriesByPlaylist(playlistId: Long): Flow<List<String>>

    @Query("SELECT * FROM iptv_channels WHERE playlistId = :playlistId AND groupTitle = :category ORDER BY name ASC")
    fun getChannelsByCategoryAndPlaylist(category: String, playlistId: Long): Flow<List<IptvChannel>>

    @Query("SELECT * FROM iptv_channels WHERE playlistId = :playlistId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchChannelsByPlaylist(query: String, playlistId: Long): Flow<List<IptvChannel>>

    @Query("UPDATE iptv_channels SET isFavorite = :isFav WHERE id = :channelId")
    suspend fun setFavorite(channelId: Long, isFav: Boolean)

    @Query("UPDATE iptv_channels SET isFavorite = :isFav WHERE (streamUrl = :streamUrl AND :streamUrl != '') OR id = :channelId")
    suspend fun setFavoriteByStreamUrl(channelId: Long, streamUrl: String, isFav: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<IptvChannel>)

    @Query("DELETE FROM iptv_channels WHERE playlistId = :playlistId AND isFavorite = 0")
    suspend fun clearNonFavoritesByPlaylist(playlistId: Long)

    @Query("DELETE FROM iptv_channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsForPlaylist(playlistId: Long)

    @Query("DELETE FROM iptv_channels")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM iptv_channels WHERE playlistId = :playlistId")
    suspend fun getCountByPlaylist(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM iptv_channels")
    suspend fun getCount(): Int

    // Playlist Management
    @Query("SELECT * FROM iptv_playlists ORDER BY id ASC")
    fun getAllPlaylists(): Flow<List<IptvPlaylist>>

    @Query("SELECT * FROM iptv_playlists WHERE isActive = 1 LIMIT 1")
    fun getActivePlaylist(): Flow<IptvPlaylist?>

    @Query("SELECT * FROM iptv_playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): IptvPlaylist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: IptvPlaylist): Long

    @Update
    suspend fun updatePlaylist(playlist: IptvPlaylist)

    @Query("DELETE FROM iptv_playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM iptv_playlists")
    suspend fun clearAllPlaylists()

    @Query("UPDATE iptv_playlists SET isActive = (id = :activeId)")
    suspend fun setActivePlaylistInternal(activeId: Long)

    @Query("SELECT COUNT(*) FROM iptv_playlists")
    suspend fun getPlaylistCount(): Int
}
