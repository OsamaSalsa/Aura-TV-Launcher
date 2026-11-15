package com.example.data

import com.example.data.model.IptvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

data class ParsedPlaylistResult(
    val channels: List<IptvChannel>,
    val epgUrl: String = ""
)

object IptvParser {

    private val GROUP_TITLE_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"")
    private val TVG_LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"")
    private val TVG_NAME_PATTERN = Pattern.compile("tvg-name=\"([^\"]*)\"")
    private val TVG_ID_PATTERN = Pattern.compile("tvg-id=\"([^\"]*)\"")
    private val URL_TVG_PATTERN = Pattern.compile("(?:url-tvg|x-tvg-url)=\"([^\"]*)\"")

    suspend fun parseFromUrl(playlistUrl: String): List<IptvChannel> {
        return parsePlaylistFromUrl(playlistUrl).channels
    }

    suspend fun parsePlaylistFromUrl(playlistUrl: String): ParsedPlaylistResult = withContext(Dispatchers.IO) {
        val url = URL(playlistUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 20000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            setRequestProperty("Accept", "*/*")
        }

        try {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                parseReaderInternal(reader)
            }
        } finally {
            conn.disconnect()
        }
    }

    fun parseFromContent(m3uContent: String): List<IptvChannel> {
        return BufferedReader(StringReader(m3uContent)).use { reader ->
            parseReaderInternal(reader).channels
        }
    }

    private fun parseReaderInternal(reader: BufferedReader): ParsedPlaylistResult {
        val channels = mutableListOf<IptvChannel>()
        var epgUrl = ""
        var currentName = ""
        var currentLogo = ""
        var currentGroup = "General"
        var currentTvgId = ""
        var order = 0

        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTM3U", ignoreCase = true)) {
                val epgMatcher = URL_TVG_PATTERN.matcher(trimmed)
                if (epgMatcher.find()) {
                    epgUrl = epgMatcher.group(1).orEmpty().substringBefore(",").trim()
                }
            } else if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                val groupMatcher = GROUP_TITLE_PATTERN.matcher(trimmed)
                currentGroup = if (groupMatcher.find()) groupMatcher.group(1).orEmpty() else "General"
                if (currentGroup.isBlank()) currentGroup = "General"

                val logoMatcher = TVG_LOGO_PATTERN.matcher(trimmed)
                currentLogo = if (logoMatcher.find()) logoMatcher.group(1).orEmpty() else ""

                val idMatcher = TVG_ID_PATTERN.matcher(trimmed)
                currentTvgId = if (idMatcher.find()) idMatcher.group(1).orEmpty().trim() else ""

                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    val nameMatcher = TVG_NAME_PATTERN.matcher(trimmed)
                    if (nameMatcher.find()) nameMatcher.group(1).orEmpty() else "Channel ${order + 1}"
                }
                if (currentTvgId.isBlank()) {
                    val nameMatcher = TVG_NAME_PATTERN.matcher(trimmed)
                    if (nameMatcher.find()) currentTvgId = nameMatcher.group(1).orEmpty().trim()
                }
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                if (currentName.isBlank()) {
                    currentName = "Channel ${order + 1}"
                }
                order++
                channels.add(
                    IptvChannel(
                        name = currentName,
                        logoUrl = currentLogo,
                        groupTitle = currentGroup,
                        streamUrl = trimmed,
                        isFavorite = false,
                        orderIndex = order,
                        tvgId = currentTvgId
                    )
                )
                currentName = ""
                currentLogo = ""
                currentGroup = "General"
                currentTvgId = ""
            }
            line = reader.readLine()
        }
        return ParsedPlaylistResult(channels = channels, epgUrl = epgUrl)
    }
}
