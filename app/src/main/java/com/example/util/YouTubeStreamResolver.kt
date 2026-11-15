package com.example.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "YouTubeStreamResolver"

private const val UA_IOS =
    "com.google.ios.youtube/20.10.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X)"
private const val UA_ANDROID_VR =
    "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 12; en_US; Quest 3; Build/SQ3A.220605.009.A1) gzip"
private const val UA_WEB =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

data class ResolvedYouTubeStream(
    val videoId: String,
    val videoUrl: String,
    val audioUrl: String? = null,
    val isHls: Boolean = false,
    val userAgent: String = UA_IOS,
    val height: Int = 1080,
    val expiresAtMs: Long = System.currentTimeMillis() + 3_600_000L
) {
    fun isStillValid(nowMs: Long = System.currentTimeMillis()): Boolean {
        return videoUrl.isNotBlank() && nowMs < (expiresAtMs - 300_000L)
    }
}

object YouTubeStreamResolver {
    private val cache = ConcurrentHashMap<String, ResolvedYouTubeStream>()

    fun getCached(videoId: String): ResolvedYouTubeStream? {
        val hit = cache[videoId] ?: return null
        return if (hit.isStillValid()) hit else {
            cache.remove(videoId)
            null
        }
    }

    fun invalidate(videoId: String) {
        cache.remove(videoId)
    }

    suspend fun prefetch(rawVideoInput: String) {
        val clean = rawVideoInput.trim()
        if (clean.isEmpty()) return
        val isDirectStream = (clean.startsWith("http://") || clean.startsWith("https://")) &&
            !clean.contains("youtube.com") && !clean.contains("youtu.be")
        if (isDirectStream) return
        val videoId = extractVideoId(clean)
        if (getCached(videoId) != null) return
        resolve(videoId, maxWaitMs = 4000L)
    }

    suspend fun resolve(videoId: String, maxWaitMs: Long = 2800L): ResolvedYouTubeStream? {
        getCached(videoId)?.let { return it }

        return withTimeoutOrNull(maxWaitMs) {
            withContext(Dispatchers.IO) {
                getCached(videoId)?.let { return@withContext it }

                coroutineScope {
                    val iosJob = async { fetchInnerTubeIos(videoId) }
                    val vrJob = async { fetchInnerTubeAndroidVr(videoId) }

                    // Prefer IOS (usually provides fast segmented 1080p HLS) first
                    val iosResult = iosJob.await()
                    if (iosResult != null) {
                        vrJob.cancel()
                        cache[videoId] = iosResult
                        return@coroutineScope iosResult
                    }

                    val vrResult = vrJob.await()
                    if (vrResult != null) {
                        cache[videoId] = vrResult
                        return@coroutineScope vrResult
                    }

                    // Fast Piped public JSON API fallback only if time remains
                    fetchPipedStream(videoId)?.let { piped ->
                        cache[videoId] = piped
                        return@coroutineScope piped
                    }

                    null
                }
            }
        }
    }

    private fun extractVideoId(input: String): String {
        val clean = input.trim()
        return when {
            clean.contains("youtu.be/") -> clean.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
            clean.contains("watch?v=") -> clean.substringAfter("watch?v=").substringBefore("&")
            clean.contains("embed/") -> clean.substringAfter("embed/").substringBefore("?").substringBefore("&")
            clean.contains("live/") -> clean.substringAfter("live/").substringBefore("?").substringBefore("&")
            clean.length in 8..15 && !clean.contains("/") -> clean
            else -> if (clean.isNotEmpty()) clean else "EG_g7BFaL5s"
        }
    }

    private fun fetchInnerTubeIos(videoId: String): ResolvedYouTubeStream? {
        val payload = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "IOS")
                    put("clientVersion", "20.10.4")
                    put("deviceMake", "Apple")
                    put("deviceModel", "iPhone16,2")
                    put("osName", "iPhone")
                    put("osVersion", "18.3.2.22D82")
                    put("hl", "en")
                })
            })
        }
        val headers = mapOf(
            "User-Agent" to UA_IOS,
            "X-YouTube-Client-Name" to "5",
            "X-YouTube-Client-Version" to "20.10.4"
        )
        return requestInnerTubePlayer(videoId, payload, headers, UA_IOS)
    }

    private fun fetchInnerTubeAndroidVr(videoId: String): ResolvedYouTubeStream? {
        val payload = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "ANDROID_VR")
                    put("clientVersion", "1.60.19")
                    put("deviceMake", "Oculus")
                    put("deviceModel", "Quest 3")
                    put("osName", "Android")
                    put("osVersion", "12")
                    put("androidSdkVersion", 32)
                    put("hl", "en")
                })
            })
        }
        val headers = mapOf(
            "User-Agent" to UA_ANDROID_VR,
            "X-YouTube-Client-Name" to "28",
            "X-YouTube-Client-Version" to "1.60.19"
        )
        return requestInnerTubePlayer(videoId, payload, headers, UA_ANDROID_VR)
    }

    private fun requestInnerTubePlayer(
        videoId: String,
        payload: JSONObject,
        headers: Map<String, String>,
        streamUa: String
    ): ResolvedYouTubeStream? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 2200
                readTimeout = 2400
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
            }

            if (conn.responseCode !in 200..299) {
                return null
            }

            val body = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            val json = JSONObject(body)
            val playability = json.optJSONObject("playabilityStatus")?.optString("status")
            if (playability != "OK") {
                return null
            }

            val streamingData = json.optJSONObject("streamingData") ?: return null
            val expiresInSec = streamingData.optString("expiresInSeconds", "3600").toLongOrNull() ?: 3600L
            val expiresAtMs = System.currentTimeMillis() + (expiresInSec * 1000L)

            val hlsManifestUrl = streamingData.optString("hlsManifestUrl", "")
            val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats")
            val muxedFormats = streamingData.optJSONArray("formats")

            if (hlsManifestUrl.isNotBlank()) {
                return ResolvedYouTubeStream(
                    videoId = videoId,
                    videoUrl = hlsManifestUrl,
                    audioUrl = null,
                    isHls = true,
                    userAgent = streamUa,
                    height = 1080,
                    expiresAtMs = expiresAtMs
                )
            }

            val bestAdaptive = pickBestAdaptivePair(adaptiveFormats)
            if (bestAdaptive != null) {
                return ResolvedYouTubeStream(
                    videoId = videoId,
                    videoUrl = bestAdaptive.first.url,
                    audioUrl = bestAdaptive.second.url,
                    isHls = false,
                    userAgent = streamUa,
                    height = bestAdaptive.first.height,
                    expiresAtMs = expiresAtMs
                )
            }

            val bestMuxed = pickBestMuxedFormat(muxedFormats)
            if (bestMuxed != null) {
                return ResolvedYouTubeStream(
                    videoId = videoId,
                    videoUrl = bestMuxed.url,
                    audioUrl = null,
                    isHls = false,
                    userAgent = streamUa,
                    height = bestMuxed.height,
                    expiresAtMs = expiresAtMs
                )
            }

            null
        } catch (e: Exception) {
            Log.w(TAG, "InnerTube resolution error for $videoId", e)
            null
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    private data class FormatCandidate(
        val url: String,
        val mimeType: String,
        val height: Int,
        val bitrate: Int
    )

    private fun pickBestAdaptivePair(adaptiveFormats: JSONArray?): Pair<FormatCandidate, FormatCandidate>? {
        if (adaptiveFormats == null || adaptiveFormats.length() == 0) return null
        val videos = mutableListOf<FormatCandidate>()
        val audios = mutableListOf<FormatCandidate>()

        for (i in 0 until adaptiveFormats.length()) {
            val obj = adaptiveFormats.optJSONObject(i) ?: continue
            val directUrl = obj.optString("url", "")
            if (directUrl.isBlank()) continue
            val mime = obj.optString("mimeType", "").lowercase()
            val bitrate = obj.optInt("bitrate", 0)
            if (mime.startsWith("video/")) {
                val height = obj.optInt("height", 0)
                if (height in 360..1080) {
                    videos.add(FormatCandidate(directUrl, mime, height, bitrate))
                }
            } else if (mime.startsWith("audio/")) {
                audios.add(FormatCandidate(directUrl, mime, 0, bitrate))
            }
        }

        if (videos.isEmpty() || audios.isEmpty()) return null

        val bestVideo = videos.maxByOrNull { v ->
            val isAvcMp4 = v.mimeType.contains("video/mp4") && v.mimeType.contains("avc1")
            val isMp4 = v.mimeType.contains("video/mp4")
            val codecBonus = when {
                isAvcMp4 -> 10_000_000L
                isMp4 -> 5_000_000L
                else -> 0L
            }
            val heightScore = v.height.toLong() * 1_000L
            codecBonus + heightScore + (v.bitrate / 1000L)
        } ?: return null

        val bestAudio = audios.maxByOrNull { a ->
            val isMp4a = a.mimeType.contains("audio/mp4") || a.mimeType.contains("mp4a")
            val codecBonus = if (isMp4a) 10_000_000L else 0L
            codecBonus + (a.bitrate / 1000L)
        } ?: return null

        return bestVideo to bestAudio
    }

    private fun pickBestMuxedFormat(formats: JSONArray?): FormatCandidate? {
        if (formats == null || formats.length() == 0) return null
        val candidates = mutableListOf<FormatCandidate>()
        for (i in 0 until formats.length()) {
            val obj = formats.optJSONObject(i) ?: continue
            val directUrl = obj.optString("url", "")
            if (directUrl.isBlank()) continue
            val mime = obj.optString("mimeType", "").lowercase()
            val height = obj.optInt("height", 0)
            val bitrate = obj.optInt("bitrate", 0)
            candidates.add(FormatCandidate(directUrl, mime, height, bitrate))
        }
        return candidates.maxByOrNull { (it.height.toLong() * 1000L) + (it.bitrate / 1000L) }
    }

    private fun fetchPipedStream(videoId: String): ResolvedYouTubeStream? {
        val hosts = listOf(
            "https://api.piped.private.coffee",
            "https://pipedapi.kavin.rocks"
        )
        for (host in hosts) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$host/streams/$videoId")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 1800
                    readTimeout = 2000
                    setRequestProperty("User-Agent", UA_WEB)
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode !in 200..299) continue
                val body = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                val json = JSONObject(body)

                val hlsUrl = json.optString("hls", "")
                if (hlsUrl.isNotBlank()) {
                    return ResolvedYouTubeStream(
                        videoId = videoId,
                        videoUrl = hlsUrl,
                        audioUrl = null,
                        isHls = true,
                        userAgent = UA_WEB,
                        height = 1080
                    )
                }

                val videoStreams = json.optJSONArray("videoStreams")
                val audioStreams = json.optJSONArray("audioStreams")

                var bestVidUrl = ""
                var bestVidHeight = 0
                var bestVidScore = -1L
                if (videoStreams != null) {
                    for (i in 0 until videoStreams.length()) {
                        val v = videoStreams.optJSONObject(i) ?: continue
                        val vUrl = v.optString("url", "")
                        if (vUrl.isBlank()) continue
                        val height = v.optInt("height", 0)
                        if (height !in 360..1080) continue
                        val mime = v.optString("mimeType", "").lowercase()
                        val codec = v.optString("codec", "").lowercase()
                        val isAvc = mime.contains("mp4") && (codec.contains("avc") || codec.isBlank())
                        val score = (if (isAvc) 10_000_000L else 0L) + height * 1000L + (v.optInt("bitrate", 0) / 1000L)
                        if (score > bestVidScore) {
                            bestVidScore = score
                            bestVidUrl = vUrl
                            bestVidHeight = height
                        }
                    }
                }

                var bestAudUrl = ""
                var bestAudScore = -1L
                if (audioStreams != null) {
                    for (i in 0 until audioStreams.length()) {
                        val a = audioStreams.optJSONObject(i) ?: continue
                        val aUrl = a.optString("url", "")
                        if (aUrl.isBlank()) continue
                        val mime = a.optString("mimeType", "").lowercase()
                        val isM4a = mime.contains("mp4") || mime.contains("aac")
                        val score = (if (isM4a) 10_000_000L else 0L) + (a.optInt("bitrate", 0) / 1000L)
                        if (score > bestAudScore) {
                            bestAudScore = score
                            bestAudUrl = aUrl
                        }
                    }
                }

                if (bestVidUrl.isNotBlank() && bestAudUrl.isNotBlank()) {
                    return ResolvedYouTubeStream(
                        videoId = videoId,
                        videoUrl = bestVidUrl,
                        audioUrl = bestAudUrl,
                        isHls = false,
                        userAgent = UA_WEB,
                        height = bestVidHeight
                    )
                }
            } catch (_: Exception) {
                // try next host
            } finally {
                try { conn?.disconnect() } catch (_: Exception) {}
            }
        }
        return null
    }
}
