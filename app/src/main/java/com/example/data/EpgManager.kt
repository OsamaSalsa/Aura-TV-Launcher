package com.example.data

import android.util.Xml
import com.example.data.model.IptvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.zip.GZIPInputStream

data class EpgProgram(
    val channelId: String,
    val title: String,
    val description: String = "",
    val startTimeMs: Long,
    val stopTimeMs: Long
)

data class ChannelEpgInfo(
    val current: EpgProgram? = null,
    val next: EpgProgram? = null
) {
    fun progressFraction(nowMs: Long = System.currentTimeMillis()): Float {
        val prog = current ?: return 0f
        val duration = (prog.stopTimeMs - prog.startTimeMs).coerceAtLeast(1L)
        return ((nowMs - prog.startTimeMs).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }

    fun formatTimeRange(): String {
        val prog = current ?: return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return "${sdf.format(prog.startTimeMs)} - ${sdf.format(prog.stopTimeMs)}"
    }
}

object EpgManager {

    private val mutex = Mutex()
    private val _epgState = MutableStateFlow<Map<String, List<EpgProgram>>>(emptyMap())
    val epgState: StateFlow<Map<String, List<EpgProgram>>> = _epgState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val displayNameToChannelId = mutableMapOf<String, String>()
    private var lastLoadedUrl: String = ""
    private var lastLoadedTimestamp: Long = 0L

    private const val CACHE_TTL_MS = 30 * 60 * 1000L

    suspend fun loadEpgIfNeeded(url: String, force: Boolean = false): Result<Int> {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return Result.success(0)

        val now = System.currentTimeMillis()
        if (!force &&
            cleanUrl == lastLoadedUrl &&
            _epgState.value.isNotEmpty() &&
            (now - lastLoadedTimestamp) < CACHE_TTL_MS
        ) {
            return Result.success(_epgState.value.size)
        }

        return mutex.withLock {
            if (!force &&
                cleanUrl == lastLoadedUrl &&
                _epgState.value.isNotEmpty() &&
                (System.currentTimeMillis() - lastLoadedTimestamp) < CACHE_TTL_MS
            ) {
                return@withLock Result.success(_epgState.value.size)
            }

            _isLoading.value = true
            try {
                val parsed = withContext(Dispatchers.IO) {
                    fetchAndParseXmltv(cleanUrl)
                }
                _epgState.value = parsed.first
                displayNameToChannelId.clear()
                displayNameToChannelId.putAll(parsed.second)
                lastLoadedUrl = cleanUrl
                lastLoadedTimestamp = System.currentTimeMillis()
                Result.success(parsed.first.size)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getChannelNowNext(
        channel: IptvChannel,
        nowMs: Long = System.currentTimeMillis(),
        programsById: Map<String, List<EpgProgram>> = _epgState.value
    ): ChannelEpgInfo {
        if (programsById.isEmpty()) return ChannelEpgInfo()

        val candidates = buildList {
            if (channel.tvgId.isNotBlank()) {
                add(normalizeKey(channel.tvgId))
            }
            val normName = normalizeKey(channel.name)
            if (normName.isNotBlank()) {
                add(normName)
                displayNameToChannelId[normName]?.let { add(it) }
            }
        }

        var list: List<EpgProgram>? = null
        for (key in candidates) {
            list = programsById[key]
            if (!list.isNullOrEmpty()) break
        }
        if (list.isNullOrEmpty()) return ChannelEpgInfo()

        var current: EpgProgram? = null
        var next: EpgProgram? = null
        for (item in list) {
            if (nowMs in item.startTimeMs..item.stopTimeMs) {
                current = item
            } else if (item.startTimeMs > nowMs) {
                if (next == null || item.startTimeMs < next.startTimeMs) {
                    next = item
                }
            }
        }
        return ChannelEpgInfo(current = current, next = next)
    }

    private fun fetchAndParseXmltv(epgUrl: String): Pair<Map<String, List<EpgProgram>>, Map<String, String>> {
        val url = URL(epgUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 25000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            setRequestProperty("Accept-Encoding", "gzip")
        }

        return try {
            val rawStream = BufferedInputStream(conn.inputStream, 16 * 1024)
            rawStream.mark(4)
            val b0 = rawStream.read()
            val b1 = rawStream.read()
            rawStream.reset()
            val isGzip = (b0 == 0x1f && b1 == 0x8b) ||
                epgUrl.endsWith(".gz", ignoreCase = true) ||
                conn.contentEncoding.equals("gzip", ignoreCase = true)

            val stream: InputStream = if (isGzip) GZIPInputStream(rawStream) else rawStream
            stream.use { parseXmltvStream(it) }
        } finally {
            conn.disconnect()
        }
    }

    private fun parseXmltvStream(input: InputStream): Pair<Map<String, List<EpgProgram>>, Map<String, String>> {
        val now = System.currentTimeMillis()
        val minStartWindow = now - 6 * 3600_000L
        val maxStartWindow = now + 18 * 3600_000L

        val programs = HashMap<String, MutableList<EpgProgram>>()
        val aliasMap = HashMap<String, String>()

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)

        var currentChannelDeclId: String? = null
        var currentProgChannelId: String? = null
        var currentStartMs = 0L
        var currentStopMs = 0L
        var currentTitle = ""
        var currentDesc = ""
        var activeTag = ""
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    activeTag = parser.name?.lowercase(Locale.US).orEmpty()
                    when (activeTag) {
                        "channel" -> {
                            currentChannelDeclId = normalizeKey(parser.getAttributeValue(null, "id").orEmpty())
                        }
                        "programme" -> {
                            val chId = normalizeKey(parser.getAttributeValue(null, "channel").orEmpty())
                            val startRaw = parser.getAttributeValue(null, "start").orEmpty()
                            val stopRaw = parser.getAttributeValue(null, "stop").orEmpty()
                            val startMs = parseXmltvTime(startRaw)
                            val stopMs = parseXmltvTime(stopRaw).let { if (it > startMs) it else startMs + 3600_000L }

                            if (chId.isNotBlank() && startMs in minStartWindow..maxStartWindow) {
                                currentProgChannelId = chId
                                currentStartMs = startMs
                                currentStopMs = stopMs
                                currentTitle = ""
                                currentDesc = ""
                            } else {
                                currentProgChannelId = null
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val txt = parser.text?.trim().orEmpty()
                    if (txt.isNotEmpty()) {
                        when {
                            currentChannelDeclId != null && activeTag == "display-name" -> {
                                val normDisplay = normalizeKey(txt)
                                if (normDisplay.isNotBlank()) {
                                    aliasMap[normDisplay] = currentChannelDeclId!!
                                }
                            }
                            currentProgChannelId != null && activeTag == "title" -> {
                                if (currentTitle.isEmpty()) currentTitle = txt
                            }
                            currentProgChannelId != null && activeTag == "desc" -> {
                                if (currentDesc.isEmpty()) currentDesc = txt
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name?.lowercase(Locale.US)) {
                        "channel" -> currentChannelDeclId = null
                        "programme" -> {
                            val chId = currentProgChannelId
                            if (chId != null && currentTitle.isNotBlank()) {
                                programs.getOrPut(chId) { mutableListOf() }.add(
                                    EpgProgram(
                                        channelId = chId,
                                        title = currentTitle,
                                        description = currentDesc,
                                        startTimeMs = currentStartMs,
                                        stopTimeMs = currentStopMs
                                    )
                                )
                            }
                            currentProgChannelId = null
                        }
                    }
                    activeTag = ""
                }
            }
            eventType = parser.next()
        }

        programs.values.forEach { list -> list.sortBy { it.startTimeMs } }
        return programs to aliasMap
    }

    private fun parseXmltvTime(raw: String): Long {
        val clean = raw.trim()
        if (clean.length < 14) return 0L
        return try {
            val digits = clean.substring(0, 14)
            val tzPart = clean.substring(14).trim()
            val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            if (tzPart.matches(Regex("[+-]\\d{4}"))) {
                sdf.timeZone = TimeZone.getTimeZone("GMT$tzPart")
            } else {
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(digits)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun normalizeKey(raw: String): String {
        return raw.trim().lowercase(Locale.US).replace(Regex("\\s+"), " ")
    }
}
