package com.example.ui.common

import android.net.Uri
import android.webkit.WebView
import java.net.URL
import java.net.URLEncoder

/**
 * Shared helper for Aura Store and Mini Downloader to support downloading APK files
 * hosted on Google Drive, Google Docs, drive.usercontent.google.com, MediaFire,
 * and intent:// fallback links.
 */
object DownloadUrlResolver {

    const val DESKTOP_CHROME_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    data class HtmlRedirectResult(
        val nextUrl: String?,
        val discoveredFileName: String? = null
    )

    /**
     * Unwraps Android `intent://` URLs into standard `https://` URLs so WebView never fails
     * with ERR_UNKNOWN_URL_SCHEME when Google Drive or APK hosts attempt to launch a native app.
     */
    fun unwrapIntentUrl(rawUrl: String): String {
        if (!rawUrl.startsWith("intent://", ignoreCase = true)) return rawUrl
        try {
            val fallbackMatch = Regex("""S\.browser_fallback_url=([^;#]+)""", RegexOption.IGNORE_CASE)
                .find(rawUrl)
            if (fallbackMatch != null) {
                val decoded = Uri.decode(fallbackMatch.groupValues[1])
                if (decoded.startsWith("http://", ignoreCase = true) ||
                    decoded.startsWith("https://", ignoreCase = true)
                ) {
                    return decoded
                }
            }
            val schemeMatch = Regex("""scheme=([a-zA-Z0-9+.-]+)""", RegexOption.IGNORE_CASE)
                .find(rawUrl)
            val scheme = schemeMatch?.groupValues?.get(1)?.lowercase() ?: "https"
            val bodyBeforeIntent = rawUrl.removePrefix("intent://").removePrefix("INTENT://")
                .substringBefore("#Intent")
            if (bodyBeforeIntent.isNotBlank()) {
                return "$scheme://$bodyBeforeIntent"
            }
        } catch (_: Exception) {}
        return rawUrl
    }

    /**
     * Extracts the Google Drive File ID if [rawUrl] is a Google Drive / Docs / UserContent link.
     */
    fun extractGoogleDriveFileId(rawUrl: String): String? {
        val url = unwrapIntentUrl(rawUrl)
        val lower = url.lowercase()
        val isGoogleDriveDomain = lower.contains("drive.google.com") ||
                lower.contains("docs.google.com") ||
                lower.contains("drive.usercontent.google.com")
        if (!isGoogleDriveDomain) return null

        // 1. /file/d/{FILE_ID} or /d/{FILE_ID}
        val pathMatcher = Regex("""/(?:file/)?d/([a-zA-Z0-9_-]{10,})""")
            .find(url)
        if (pathMatcher != null) {
            return pathMatcher.groupValues[1]
        }

        // 2. ?id={FILE_ID} or &id={FILE_ID}
        val queryMatcher = Regex("""[?&]id=([a-zA-Z0-9_-]{10,})""")
            .find(url)
        if (queryMatcher != null) {
            return queryMatcher.groupValues[1]
        }

        return null
    }

    /**
     * Returns true if the URL is a direct APK link or a Google Drive / Docs file download link
     * that should be intercepted by the native APK downloader.
     */
    fun shouldInterceptAsDownload(rawUrl: String): Boolean {
        val url = unwrapIntentUrl(rawUrl)
        val isApk = url.endsWith(".apk", ignoreCase = true) ||
                url.contains(".apk?", ignoreCase = true) ||
                url.contains(".apk&", ignoreCase = true) ||
                url.contains("-apk.", ignoreCase = true)
        if (isApk) return true

        val driveId = extractGoogleDriveFileId(url)
        if (!driveId.isNullOrBlank()) return true

        if (url.contains("drive.usercontent.google.com/download", ignoreCase = true)) {
            return true
        }

        return false
    }

    /**
     * Converts Google Drive view/open/uc URLs into the direct download endpoint URL.
     */
    fun normalizeInitialDownloadUrl(rawUrl: String): String {
        val url = unwrapIntentUrl(rawUrl)
        val driveId = extractGoogleDriveFileId(url)
        if (!driveId.isNullOrBlank()) {
            // If it already has a confirmation uuid on drive.usercontent.google.com, preserve it
            if (url.contains("drive.usercontent.google.com/download", ignoreCase = true) &&
                url.contains("uuid=", ignoreCase = true)
            ) {
                return url
            }
            return "https://drive.usercontent.google.com/download?id=$driveId&export=download&confirm=t"
        }
        return url
    }

    /**
     * Selects the appropriate User-Agent for the given URL.
     * Google Drive requires a Desktop User-Agent so it does not redirect to Android intent:// schemes.
     */
    fun resolveUserAgentForUrl(url: String, defaultUserAgent: String): String {
        val lower = url.lowercase()
        return if (lower.contains("drive.google.com") ||
            lower.contains("docs.google.com") ||
            lower.contains("googleusercontent.com")
        ) {
            DESKTOP_CHROME_USER_AGENT
        } else {
            defaultUserAgent
        }
    }

    /**
     * Parses HTML intermediate pages (Google Drive Virus Scan Warning form, MediaFire, AFTVnews, etc.)
     * to extract the direct binary download URL and any discovered filename.
     */
    fun parseHtmlDownloadPage(currentUrl: String, htmlBody: String): HtmlRedirectResult {
        if (htmlBody.isBlank()) return HtmlRedirectResult(null, null)

        // 1. Discover filename from Google Drive's virus-scan warning page if present:
        //    <span class="uc-name-size"><a href="...">App_Name.apk</a>
        var discoveredName: String? = null
        val driveNameMatch = Regex(
            """class=["']uc-name-size["'][^>]*>\s*(?:<a[^>]*>)?([^<()]+?)(?:</a>|\s*\()""",
            RegexOption.IGNORE_CASE
        ).find(htmlBody)
        if (driveNameMatch != null) {
            val candidate = driveNameMatch.groupValues[1].trim()
            if (candidate.isNotEmpty()) {
                discoveredName = candidate
            }
        }

        // 2. Check for Google Drive <form id="download-form" action="..."> with hidden inputs
        val formMatcher = Regex(
            """<form[^>]*id=["']download-form["'][^>]*action=["']([^"']+)["'][^>]*>(.*?)</form>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(htmlBody) ?: Regex(
            """<form[^>]*action=["'](https://drive\.usercontent\.google\.com/download[^"']*)["'][^>]*>(.*?)</form>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(htmlBody)

        if (formMatcher != null) {
            val actionRaw = formMatcher.groupValues[1].replace("&amp;", "&").trim()
            val formInner = formMatcher.groupValues[2]
            val params = linkedMapOf<String, String>()

            val inputRegex = Regex("""<input\b[^>]*>""", RegexOption.IGNORE_CASE)
            val nameRegex = Regex("""\bname=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            val valueRegex = Regex("""\bvalue=["']([^"']*)["']""", RegexOption.IGNORE_CASE)

            for (inputMatch in inputRegex.findAll(formInner)) {
                val tag = inputMatch.value
                val name = nameRegex.find(tag)?.groupValues?.get(1)?.trim()
                val value = valueRegex.find(tag)?.groupValues?.get(1)?.trim() ?: ""
                if (!name.isNullOrEmpty() && !name.equals("submit", ignoreCase = true)) {
                    params[name] = value
                }
            }

            val driveId = params["id"] ?: extractGoogleDriveFileId(currentUrl)
            if (!driveId.isNullOrBlank()) {
                params.putIfAbsent("id", driveId)
                params.putIfAbsent("export", "download")
                params.putIfAbsent("confirm", "t")
                val query = params.entries.joinToString("&") { (k, v) ->
                    "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
                }
                val baseAction = actionRaw.substringBefore("?")
                return HtmlRedirectResult("$baseAction?$query", discoveredName)
            }
        }

        // 3. Check for Google Drive confirm/uc link in href or query parameters
        val driveUcLink = Regex(
            """href=["']((?:https://drive\.usercontent\.google\.com)?/(?:uc|download)\?[^"']*export=download[^"']*)["']""",
            RegexOption.IGNORE_CASE
        ).find(htmlBody)?.groupValues?.get(1)?.replace("&amp;", "&")
        if (!driveUcLink.isNullOrBlank()) {
            val resolved = if (driveUcLink.startsWith("http", ignoreCase = true)) {
                driveUcLink
            } else {
                URL(URL(currentUrl), driveUcLink).toString()
            }
            return HtmlRedirectResult(resolved, discoveredName)
        }

        // 4. Check for MediaFire direct download button
        val mediaFireLink = Regex(
            """id=["']downloadButton["'][^>]*href=["'](https?://[^"']+)["']""",
            RegexOption.IGNORE_CASE
        ).find(htmlBody)?.groupValues?.get(1) ?: Regex(
            """href=["'](https?://download[0-9]*\.mediafire\.com/[^"']+)["']""",
            RegexOption.IGNORE_CASE
        ).find(htmlBody)?.groupValues?.get(1)
        if (!mediaFireLink.isNullOrBlank()) {
            return HtmlRedirectResult(mediaFireLink, discoveredName)
        }

        // 5. Standard direct .apk href, AFTVnews redirect, JS location, or meta refresh
        val targetLink = Regex("""href=["']([^"']+\.apk[^"']*)["']""", RegexOption.IGNORE_CASE)
            .find(htmlBody)?.groupValues?.get(1)
            ?: Regex("""id=["']redirect_message["'][^>]*href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(htmlBody)?.groupValues?.get(1)
            ?: Regex("""window\.location(?:\.href)?\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(htmlBody)?.groupValues?.get(1)
            ?: Regex("""meta\s+http-equiv=["']refresh["']\s+content=["'][^"']*url=([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(htmlBody)?.groupValues?.get(1)

        if (!targetLink.isNullOrBlank()) {
            val cleanLink = targetLink.replace("&amp;", "&").trim()
            val resolved = if (cleanLink.startsWith("http://", ignoreCase = true) ||
                cleanLink.startsWith("https://", ignoreCase = true)
            ) {
                cleanLink
            } else {
                URL(URL(currentUrl), cleanLink).toString()
            }
            return HtmlRedirectResult(resolved, discoveredName)
        }

        return HtmlRedirectResult(null, discoveredName)
    }

    /**
     * Injects a lightweight JS hook so links with `target="_blank"` or `window.open(...)`
     * (commonly used for Google Drive links inside Aura Store) navigate in the active WebView
     * and get intercepted cleanly by `shouldOverrideUrlLoading`.
     */
    fun injectPopupAndBlankTargetShim(webView: WebView?) {
        if (webView == null || isWebViewDestroyed(webView)) return
        val script = """
            (function() {
                if (window.__auraBlankTargetShimApplied) return;
                window.__auraBlankTargetShimApplied = true;
                try {
                    window.open = function(url) {
                        if (url && typeof url === 'string') {
                            window.location.href = url;
                        }
                        return window;
                    };
                } catch (e) {}
                try {
                    document.addEventListener('click', function(e) {
                        var el = e.target;
                        while (el && el.tagName !== 'A') {
                            el = el.parentElement;
                        }
                        if (el && el.tagName === 'A') {
                            if (el.getAttribute('target') === '_blank') {
                                el.removeAttribute('target');
                            }
                        }
                    }, true);
                } catch (e) {}
            })();
        """.trimIndent()
        try {
            webView.evaluateJavascript(script, null)
        } catch (_: Exception) {}
    }
}
