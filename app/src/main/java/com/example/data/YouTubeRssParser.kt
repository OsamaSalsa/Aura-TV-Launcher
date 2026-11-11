package com.example.data

import android.util.Xml
import com.example.data.model.YouTubeVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object YouTubeRssParser {

    suspend fun resolveChannelId(input: String): String = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return@withContext ""

        // 1. Direct UC ID check (YouTube channel IDs start with UC and are usually 24 chars long)
        if (trimmed.startsWith("UC") && trimmed.length == 24) {
            return@withContext trimmed
        }

        // 2. Extract from URL query parameter channel_id=
        if (trimmed.contains("channel_id=")) {
            val extracted = trimmed.substringAfter("channel_id=").substringBefore("&").substringBefore("#")
            if (extracted.isNotBlank()) return@withContext extracted
        }

        // 3. Extract from /channel/UC...
        if (trimmed.contains("/channel/")) {
            val extracted = trimmed.substringAfter("/channel/").substringBefore("/").substringBefore("?").substringBefore("#")
            if (extracted.isNotBlank()) return@withContext extracted
        }

        // 4. For handles (@SolaceTones) or URLs (https://www.youtube.com/@SolaceTones):
        val pageUrlStr = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("@") -> "https://www.youtube.com/$trimmed"
            else -> "https://www.youtube.com/@$trimmed"
        }

        try {
            val url = URL(pageUrlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
            }

            val htmlContent = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val rssPattern = Regex("""channel_id=(UC[\w-]{22})""")
            val rssMatch = rssPattern.find(htmlContent)
            if (rssMatch != null) {
                return@withContext rssMatch.groupValues[1]
            }

            val metaPattern = Regex(""""externalId":"(UC[\w-]{22})"""")
            val metaMatch = metaPattern.find(htmlContent)
            if (metaMatch != null) {
                return@withContext metaMatch.groupValues[1]
            }

            val browsePattern = Regex(""""channelId":"(UC[\w-]{22})"""")
            val browseMatch = browsePattern.find(htmlContent)
            if (browseMatch != null) {
                return@withContext browseMatch.groupValues[1]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext trimmed
    }

    suspend fun fetchChannelVideos(channelIdOrUrl: String): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        val realChannelId = resolveChannelId(channelIdOrUrl)
        if (realChannelId.isBlank()) return@withContext emptyList()

        val rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=$realChannelId"
        val url = URL(rssUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 12000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        }

        try {
            conn.inputStream.use { stream ->
                parseFeed(stream, realChannelId)
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            conn.disconnect()
        }
    }

    private fun parseFeed(inputStream: InputStream, fallbackChannelId: String): List<YouTubeVideo> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(inputStream, null)

        val videos = mutableListOf<YouTubeVideo>()
        var eventType = parser.eventType

        var currentVideoId = ""
        var currentTitle = ""
        var currentChannelTitle = ""
        var currentThumbnail = ""
        var currentPublished = ""
        var currentDescription = ""
        var inEntry = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name.equals("entry", ignoreCase = true)) {
                        inEntry = true
                        currentVideoId = ""
                        currentTitle = ""
                        currentChannelTitle = ""
                        currentThumbnail = ""
                        currentPublished = ""
                        currentDescription = ""
                    } else if (inEntry) {
                        when (name?.lowercase()) {
                            "videoid" -> currentVideoId = parser.nextText()
                            "title" -> {
                                if (currentTitle.isEmpty()) {
                                    currentTitle = parser.nextText()
                                }
                            }
                            "name" -> {
                                if (currentChannelTitle.isEmpty()) {
                                    currentChannelTitle = parser.nextText()
                                }
                            }
                            "published" -> currentPublished = parser.nextText()
                            "thumbnail" -> {
                                val urlAttr = parser.getAttributeValue(null, "url")
                                if (!urlAttr.isNullOrBlank()) {
                                    currentThumbnail = urlAttr
                                }
                            }
                            "description" -> currentDescription = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name.equals("entry", ignoreCase = true)) {
                        if (currentVideoId.isNotBlank()) {
                            val thumb = if (currentThumbnail.isNotBlank()) {
                                currentThumbnail
                            } else {
                                "https://i.ytimg.com/vi/$currentVideoId/hqdefault.jpg"
                            }
                            val cleanDate = if (currentPublished.length >= 10) currentPublished.substring(0, 10) else currentPublished
                            videos.add(
                                YouTubeVideo(
                                    videoId = currentVideoId,
                                    title = currentTitle.ifBlank { "YouTube Video" },
                                    channelTitle = currentChannelTitle,
                                    channelId = fallbackChannelId,
                                    thumbnailUrl = thumb,
                                    publishedDate = cleanDate,
                                    description = currentDescription
                                )
                            )
                        }
                        inEntry = false
                    }
                }
            }
            eventType = parser.next()
        }

        return videos
    }
}
