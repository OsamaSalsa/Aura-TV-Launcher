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

object IptvParser {

    private val GROUP_TITLE_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"")
    private val TVG_LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"")
    private val TVG_NAME_PATTERN = Pattern.compile("tvg-name=\"([^\"]*)\"")

    suspend fun parseFromUrl(playlistUrl: String): List<IptvChannel> = withContext(Dispatchers.IO) {
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
                parseReader(reader)
            }
        } finally {
            conn.disconnect()
        }
    }

    fun parseFromContent(m3uContent: String): List<IptvChannel> {
        return BufferedReader(StringReader(m3uContent)).use { reader ->
            parseReader(reader)
        }
    }

    private fun parseReader(reader: BufferedReader): List<IptvChannel> {
        val channels = mutableListOf<IptvChannel>()
        var currentName = ""
        var currentLogo = ""
        var currentGroup = "General"
        var order = 0

        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                // Extract group-title
                val groupMatcher = GROUP_TITLE_PATTERN.matcher(trimmed)
                currentGroup = if (groupMatcher.find()) groupMatcher.group(1).orEmpty() else "General"
                if (currentGroup.isBlank()) currentGroup = "General"

                // Extract logo
                val logoMatcher = TVG_LOGO_PATTERN.matcher(trimmed)
                currentLogo = if (logoMatcher.find()) logoMatcher.group(1).orEmpty() else ""

                // Extract channel name: after last comma
                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    val nameMatcher = TVG_NAME_PATTERN.matcher(trimmed)
                    if (nameMatcher.find()) nameMatcher.group(1).orEmpty() else "Channel ${order + 1}"
                }
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                // Stream URL
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
                        orderIndex = order
                    )
                )
                currentName = ""
                currentLogo = ""
                currentGroup = "General"
            }
            line = reader.readLine()
        }
        return channels
    }
}
