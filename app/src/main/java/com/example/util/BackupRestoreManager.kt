package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.data.IptvRepository
import com.example.data.local.LauncherPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupRestoreManager {

    const val BACKUP_FILE_NAME = "aura_tv_backup.json"

    private fun getCandidateFiles(context: Context): List<File> {
        val candidates = mutableListOf<File>()
        try {
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDownloads != null) {
                candidates.add(File(publicDownloads, BACKUP_FILE_NAME))
            }
        } catch (_: Exception) {}
        try {
            val appDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (appDownloads != null) {
                candidates.add(File(appDownloads, BACKUP_FILE_NAME))
            }
        } catch (_: Exception) {}
        candidates.add(File(context.filesDir, BACKUP_FILE_NAME))
        return candidates.distinctBy { it.absolutePath }
    }

    fun createBackupJson(
        prefsMap: Map<String, Any>,
        playlists: List<com.example.data.model.IptvPlaylist>
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("appVersion", "1.4")
        root.put("timestamp", System.currentTimeMillis())

        val prefsJson = JSONObject()
        for ((key, value) in prefsMap) {
            when (value) {
                is Collection<*> -> {
                    val arr = JSONArray()
                    value.forEach { item -> if (item != null) arr.put(item.toString()) }
                    prefsJson.put(key, arr)
                }
                else -> prefsJson.put(key, value)
            }
        }
        root.put("preferences", prefsJson)

        val playlistsArr = JSONArray()
        for (pl in playlists) {
            val plObj = JSONObject().apply {
                put("name", pl.name)
                put("url", pl.url)
                put("epgUrl", pl.epgUrl)
                put("isActive", pl.isActive)
            }
            playlistsArr.put(plObj)
        }
        root.put("iptvPlaylists", playlistsArr)

        return root.toString(2)
    }

    suspend fun createBackupJson(
        preferences: LauncherPreferences,
        iptvRepository: IptvRepository
    ): String = withContext(Dispatchers.IO) {
        val prefsMap = preferences.exportPreferencesSnapshot()
        val playlists = iptvRepository.playlistsFlow.first()
        createBackupJson(prefsMap, playlists)
    }

    suspend fun exportBackupToFile(
        context: Context,
        preferences: LauncherPreferences,
        iptvRepository: IptvRepository,
        targetUri: Uri? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = createBackupJson(preferences, iptvRepository)
            if (targetUri != null) {
                context.contentResolver.openOutputStream(targetUri, "wt")?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                } ?: return@withContext Result.failure(Exception("Cannot open output stream"))
                return@withContext Result.success(targetUri.lastPathSegment ?: BACKUP_FILE_NAME)
            }

            for (file in getCandidateFiles(context)) {
                try {
                    file.parentFile?.mkdirs()
                    file.writeText(json, Charsets.UTF_8)
                    return@withContext Result.success(file.absolutePath)
                } catch (_: Exception) {
                    // Try next writable location
                }
            }
            Result.failure(Exception("Could not write backup file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromBackupJson(
        jsonString: String,
        preferences: LauncherPreferences,
        iptvRepository: IptvRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val prefsObj = root.optJSONObject("preferences")
            var restoredKeys = 0
            if (prefsObj != null) {
                val map = linkedMapOf<String, Any?>()
                val keys = prefsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val raw = prefsObj.get(key)
                    if (raw is JSONArray) {
                        val list = mutableListOf<String>()
                        for (i in 0 until raw.length()) {
                            list.add(raw.optString(i))
                        }
                        map[key] = list.toSet()
                    } else {
                        map[key] = raw
                    }
                    restoredKeys++
                }
                preferences.importPreferencesSnapshot(map)
            }

            val playlistsArr = root.optJSONArray("iptvPlaylists")
            if (playlistsArr != null && playlistsArr.length() > 0) {
                val existingUrls = iptvRepository.playlistsFlow.first().map { it.url }.toSet()
                for (i in 0 until playlistsArr.length()) {
                    val obj = playlistsArr.optJSONObject(i) ?: continue
                    val name = obj.optString("name")
                    val url = obj.optString("url")
                    val isActive = obj.optBoolean("isActive", false)
                    if (url.isNotBlank() && !existingUrls.contains(url)) {
                        iptvRepository.addPlaylist(name, url, activateImmediately = isActive)
                    }
                }
            }

            Result.success(restoredKeys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackupFromFile(
        context: Context,
        preferences: LauncherPreferences,
        iptvRepository: IptvRepository,
        sourceUri: Uri? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (sourceUri != null) {
                val content = context.contentResolver.openInputStream(sourceUri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                } ?: return@withContext Result.failure(Exception("Cannot read selected backup file"))
                return@withContext restoreFromBackupJson(content, preferences, iptvRepository)
                    .map { sourceUri.lastPathSegment ?: BACKUP_FILE_NAME }
            }

            val existingFile = getCandidateFiles(context).firstOrNull { it.exists() && it.canRead() && it.length() > 2L }
                ?: return@withContext Result.failure(Exception("No backup file (aura_tv_backup.json) found in Downloads"))

            val content = existingFile.readText(Charsets.UTF_8)
            restoreFromBackupJson(content, preferences, iptvRepository).map { existingFile.absolutePath }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
