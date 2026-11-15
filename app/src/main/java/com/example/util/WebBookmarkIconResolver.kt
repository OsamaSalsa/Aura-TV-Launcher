package com.example.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object WebBookmarkIconResolver {

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    private const val MAX_HTML_BYTES = 256 * 1024
    private const val MAX_IMAGE_BYTES = 4 * 1024 * 1024

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val TAG_META_REGEX = Regex("(?is)<meta\\b[^>]*>")
    private val TAG_LINK_REGEX = Regex("(?is)<link\\b[^>]*>")
    private val ATTR_REGEX = Regex("""(?is)([a-zA-Z0-9_:-]+)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>`]+))""")

    private val SOCIAL_IMAGE_KEYS = listOf(
        "og:image:secure_url",
        "og:image",
        "og:image:url",
        "twitter:image:src",
        "twitter:image"
    )

    suspend fun resolveAndCacheIcon(
        context: Context,
        bookmarkId: String,
        rawUrl: String
    ): String? = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeHttpUrl(rawUrl) ?: return@withContext null
        val targetDir = File(context.filesDir, "bookmark_icons").apply { mkdirs() }
        val targetFile = File(targetDir, "$bookmarkId.img")

        val candidates = mutableListOf<String>()
        var finalUrl = normalizedUrl.toHttpUrlOrNull()

        // 1. Fetch HTML head to discover social preview meta images and site/browser icons
        val htmlResult = fetchHtmlSnippet(normalizedUrl)
        if (htmlResult != null) {
            finalUrl = htmlResult.first
            val html = htmlResult.second

            // Priority 1: Social meta preview image (og:image / twitter:image / image_src / itemprop=image)
            extractSocialMetaImageUrls(html, finalUrl).forEach { url ->
                if (!isUnsupportedVectorUrl(url)) candidates.add(url)
            }

            // Priority 2: Declared browser/touch icons from HTML <link> tags
            extractHtmlIconUrls(html, finalUrl).forEach { url ->
                if (!isUnsupportedVectorUrl(url)) candidates.add(url)
            }
        }

        // Priority 3: Standard browser favicon / touch-icon paths on the target host
        if (finalUrl != null) {
            val baseOrigin = "${finalUrl.scheme}://${finalUrl.host}" +
                if ((finalUrl.scheme == "https" && finalUrl.port != 443) ||
                    (finalUrl.scheme == "http" && finalUrl.port != 80)
                ) ":${finalUrl.port}" else ""
            candidates.add("$baseOrigin/apple-touch-icon.png")
            candidates.add("$baseOrigin/favicon.ico")
            // Guaranteed crisp browser icon fallback
            candidates.add(
                "https://www.google.com/s2/favicons?domain=${Uri.encode(finalUrl.host)}&sz=256"
            )
        }

        // Try candidates in strict priority order until a valid decodable raster image is downloaded
        for (candidateUrl in candidates.distinct()) {
            val bytes = downloadValidImageBytes(candidateUrl)
            if (bytes != null) {
                try {
                    targetFile.writeBytes(bytes)
                    return@withContext targetFile.absolutePath
                } catch (_: Exception) {
                    return@withContext candidateUrl
                }
            }
        }
        null
    }

    fun deleteCachedIcon(context: Context, bookmarkId: String) {
        try {
            File(context.filesDir, "bookmark_icons/$bookmarkId.img").delete()
        } catch (_: Exception) {}
    }

    private fun normalizeHttpUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return null
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun fetchHtmlSnippet(url: String): Pair<HttpUrl, String>? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val contentType = response.header("Content-Type")?.lowercase().orEmpty()
                if (contentType.isNotEmpty() &&
                    !contentType.contains("html") &&
                    !contentType.contains("xml") &&
                    !contentType.contains("text/")
                ) {
                    return null
                }
                val body = response.body ?: return null
                val stream = body.byteStream()
                val buffer = ByteArray(8192)
                val out = java.io.ByteArrayOutputStream()
                var total = 0
                while (total < MAX_HTML_BYTES) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    out.write(buffer, 0, read)
                    total += read
                    val partial = out.toString(Charsets.UTF_8.name())
                    if (partial.contains("</head>", ignoreCase = true)) break
                }
                response.request.url to out.toString(Charsets.UTF_8.name())
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAttributes(tag: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        ATTR_REGEX.findAll(tag).forEach { match ->
            val key = match.groupValues[1].lowercase()
            val value = match.groupValues[2].ifEmpty {
                match.groupValues[3].ifEmpty { match.groupValues[4] }
            }
            if (key.isNotEmpty() && value.isNotEmpty()) {
                map[key] = decodeHtmlEntities(value.trim())
            }
        }
        return map
    }

    private fun extractSocialMetaImageUrls(html: String, baseUrl: HttpUrl): List<String> {
        val foundByPriority = LinkedHashMap<String, String>()

        TAG_META_REGEX.findAll(html).forEach { match ->
            val attrs = parseAttributes(match.value)
            val key = (attrs["property"] ?: attrs["name"] ?: attrs["itemprop"] ?: "").lowercase()
            val content = attrs["content"] ?: return@forEach
            val resolved = resolveUrl(baseUrl, content) ?: return@forEach
            if (key in SOCIAL_IMAGE_KEYS || key == "image") {
                foundByPriority.putIfAbsent(key, resolved)
            }
        }

        val ordered = mutableListOf<String>()
        for (priorityKey in SOCIAL_IMAGE_KEYS) {
            foundByPriority[priorityKey]?.let { ordered.add(it) }
        }

        TAG_LINK_REGEX.findAll(html).forEach { match ->
            val attrs = parseAttributes(match.value)
            val rel = attrs["rel"]?.lowercase().orEmpty().split(Pattern.compile("\\s+"))
            if ("image_src" in rel) {
                attrs["href"]?.let { href ->
                    resolveUrl(baseUrl, href)?.let { ordered.add(it) }
                }
            }
        }

        foundByPriority["image"]?.let { ordered.add(it) }
        return ordered
    }

    private fun extractHtmlIconUrls(html: String, baseUrl: HttpUrl): List<String> {
        data class IconCandidate(val url: String, val score: Int)
        val candidates = mutableListOf<IconCandidate>()

        TAG_LINK_REGEX.findAll(html).forEach { match ->
            val attrs = parseAttributes(match.value)
            val relTokens = attrs["rel"]?.lowercase().orEmpty().split(Pattern.compile("\\s+"))
            val type = attrs["type"]?.lowercase().orEmpty()
            val href = attrs["href"] ?: return@forEach
            if (type.contains("svg")) return@forEach

            val isAppleTouch = relTokens.any {
                it == "apple-touch-icon" || it == "apple-touch-icon-precomposed"
            }
            val isIcon = relTokens.any { it == "icon" || it == "shortcut" }
            if (!isAppleTouch && !isIcon) return@forEach

            val resolved = resolveUrl(baseUrl, href) ?: return@forEach
            val sizesVal = attrs["sizes"]?.lowercase().orEmpty()
            val maxDim = Regex("(\\d+)x(\\d+)").findAll(sizesVal)
                .mapNotNull { it.groupValues[1].toIntOrNull() }
                .maxOrNull() ?: 0

            var score = 0
            if (isAppleTouch) score += 1000
            if (resolved.endsWith(".png", ignoreCase = true) ||
                resolved.endsWith(".webp", ignoreCase = true) ||
                resolved.endsWith(".jpg", ignoreCase = true) ||
                resolved.endsWith(".jpeg", ignoreCase = true)
            ) {
                score += 200
            }
            score += maxDim
            candidates.add(IconCandidate(resolved, score))
        }

        return candidates.sortedByDescending { it.score }.map { it.url }
    }

    private fun resolveUrl(baseUrl: HttpUrl, candidate: String): String? {
        val clean = candidate.trim()
        if (clean.isBlank() || clean.startsWith("data:", ignoreCase = true)) return null
        return try {
            val resolved = baseUrl.resolve(clean) ?: return null
            if (resolved.scheme == "http" || resolved.scheme == "https") resolved.toString() else null
        } catch (_: Exception) {
            null
        }
    }

    private fun isUnsupportedVectorUrl(url: String): Boolean {
        val lower = url.substringBefore('?').substringBefore('#').lowercase()
        return lower.endsWith(".svg") || lower.endsWith(".svgz")
    }

    private fun downloadValidImageBytes(imageUrl: String): ByteArray? {
        return try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "image/webp,image/png,image/jpeg,image/*,*/*;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val contentType = response.header("Content-Type")?.lowercase().orEmpty()
                if (contentType.contains("svg") || contentType.contains("html") || contentType.contains("text/")) {
                    return null
                }
                val body = response.body ?: return null
                val contentLength = body.contentLength()
                if (contentLength > MAX_IMAGE_BYTES) return null

                val bytes = body.bytes()
                if (bytes.isEmpty() || bytes.size > MAX_IMAGE_BYTES) return null

                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                if (opts.outWidth >= 16 && opts.outHeight >= 16) bytes else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&#38;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}
