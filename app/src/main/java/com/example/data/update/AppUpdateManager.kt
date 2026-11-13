package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
    val mandatory: Boolean = false,
    val sha256: String? = null
)

sealed class UpdateCheckResult {
    data object UpToDate : UpdateCheckResult()
    data class UpdateAvailable(val info: AppUpdateInfo) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

sealed class UpdateDownloadState {
    data object Idle : UpdateDownloadState()
    data class Downloading(
        val progress: Float,
        val downloadedMb: Float,
        val totalMb: Float
    ) : UpdateDownloadState()
    data object PermissionRequired : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

object AppUpdateManager {
    const val GITHUB_OWNER = "OsamaSalsa"
    const val GITHUB_REPO = "Aura-TV-Launcher"
    const val GITHUB_BRANCH = "main"

    const val DEFAULT_VERSION_JSON_URL =
        "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/$GITHUB_BRANCH/version.json"
    const val DEFAULT_BUILD_GRADLE_URL =
        "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/$GITHUB_BRANCH/app/build.gradle.kts"
    const val DEFAULT_DIRECT_APK_URL =
        "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/$GITHUB_BRANCH/aura-tv-launcher.apk"
    const val DEFAULT_RELEASES_API_URL =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    fun normalizeDownloadUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        // Convert GitHub web blob URL to raw download URL automatically
        val blobRegex = Regex("^https?://github\\.com/([^/]+)/([^/]+)/blob/([^/]+)/(.+)$")
        val match = blobRegex.matchEntire(trimmed)
        if (match != null) {
            val (owner, repo, branch, path) = match.destructured
            return "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
        }
        return trimmed
    }

    suspend fun checkForUpdates(customManifestUrl: String = ""): UpdateCheckResult = withContext(Dispatchers.IO) {
        val currentCode = BuildConfig.VERSION_CODE
        val currentName = BuildConfig.VERSION_NAME
        var lastError: String? = null

        val manifestUrl = customManifestUrl.trim().ifBlank { DEFAULT_VERSION_JSON_URL }

        // 1. Primary check: version.json on main (or custom manifest URL)
        try {
            val jsonText = fetchTextWithNoCache(manifestUrl)
            val json = JSONObject(jsonText)
            val remoteCode = json.optInt("versionCode", 0)
            val remoteName = json.optString("versionName", "").trim().ifBlank { remoteCode.toString() }
            val rawApkUrl = json.optString("apkUrl", "").trim().ifBlank { DEFAULT_DIRECT_APK_URL }
            val apkUrl = normalizeDownloadUrl(rawApkUrl)
            val changelog = json.optString("changelog", "").trim()
            val mandatory = json.optBoolean("mandatory", false)
            val sha256 = json.optString("sha256", "").trim().ifBlank { null }

            if (isRemoteNewer(remoteCode, remoteName, currentCode, currentName)) {
                return@withContext UpdateCheckResult.UpdateAvailable(
                    AppUpdateInfo(
                        versionCode = remoteCode.coerceAtLeast(currentCode + 1),
                        versionName = remoteName,
                        apkUrl = apkUrl,
                        changelog = changelog,
                        mandatory = mandatory,
                        sha256 = sha256
                    )
                )
            }
        } catch (e: Exception) {
            lastError = e.message
        }

        // Only run GitHub-specific fallbacks if using default repo or no custom URL override
        if (customManifestUrl.isBlank()) {
            // 2. Fallback check: GitHub Releases latest API (if repo publishes APK in Releases)
            try {
                val releaseJsonText = fetchTextWithNoCache(DEFAULT_RELEASES_API_URL)
                val releaseJson = JSONObject(releaseJsonText)
                val tagName = releaseJson.optString("tag_name", "").trim()
                val cleanTagVersion = tagName.removePrefix("v").removePrefix("V").trim()
                val body = releaseJson.optString("body", "").trim()
                val assets = releaseJson.optJSONArray("assets")
                var releaseApkUrl: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            releaseApkUrl = asset.optString("browser_download_url", "").ifBlank { null }
                            break
                        }
                    }
                }
                if (!releaseApkUrl.isNullOrBlank() && compareVersions(cleanTagVersion, currentName) > 0) {
                    return@withContext UpdateCheckResult.UpdateAvailable(
                        AppUpdateInfo(
                            versionCode = currentCode + 1,
                            versionName = cleanTagVersion.ifBlank { tagName },
                            apkUrl = releaseApkUrl,
                            changelog = body.ifBlank { "GitHub Release $tagName" },
                            mandatory = false
                        )
                    )
                }
            } catch (_: Exception) {
                // Releases may not exist yet; continue cleanly
            }

            // 3. Fallback check: parse versionCode & versionName directly from app/build.gradle.kts on main
            try {
                val gradleText = fetchTextWithNoCache(DEFAULT_BUILD_GRADLE_URL)
                val codeMatch = Regex("""versionCode\s*=\s*(\d+)""").find(gradleText)
                val nameMatch = Regex("""versionName\s*=\s*"([^"]+)"""").find(gradleText)
                val remoteCode = codeMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
                val remoteName = nameMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
                if (isRemoteNewer(remoteCode, remoteName, currentCode, currentName)) {
                    return@withContext UpdateCheckResult.UpdateAvailable(
                        AppUpdateInfo(
                            versionCode = remoteCode,
                            versionName = remoteName.ifBlank { remoteCode.toString() },
                            apkUrl = DEFAULT_DIRECT_APK_URL,
                            changelog = "",
                            mandatory = false
                        )
                    )
                }
                return@withContext UpdateCheckResult.UpToDate
            } catch (e: Exception) {
                if (lastError == null) lastError = e.message
            }
        }

        if (lastError != null) {
            return@withContext UpdateCheckResult.Error(lastError)
        }
        UpdateCheckResult.UpToDate
    }

    private fun isRemoteNewer(
        remoteCode: Int,
        remoteName: String,
        currentCode: Int,
        currentName: String
    ): Boolean {
        if (remoteCode > currentCode) return true
        if (remoteCode == currentCode && remoteCode > 0) {
            return compareVersions(remoteName, currentName) > 0
        }
        return compareVersions(remoteName, currentName) > 0
    }

    fun compareVersions(v1: String, v2: String): Int {
        val clean1 = v1.removePrefix("v").removePrefix("V").trim()
        val clean2 = v2.removePrefix("v").removePrefix("V").trim()
        if (clean1.isBlank() || clean2.isBlank()) return 0
        val parts1 = clean1.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        val parts2 = clean2.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }

    private fun fetchTextWithNoCache(urlStr: String): String {
        val separator = if (urlStr.contains("?")) "&" else "?"
        val finalUrl = "${normalizeDownloadUrl(urlStr)}${separator}_ts=${System.currentTimeMillis()}"
        var current = finalUrl
        repeat(5) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                instanceFollowRedirects = false
                useCaches = false
                setRequestProperty("Cache-Control", "no-cache, no-store")
                setRequestProperty("Pragma", "no-cache")
                setRequestProperty("User-Agent", "AuraTV-Updater/${BuildConfig.VERSION_NAME}")
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }
            try {
                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                        ?: throw IllegalStateException("Redirect missing Location")
                    current = URL(URL(current), location).toString()
                } else if (code in 200..299) {
                    return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } else {
                    throw IllegalStateException("HTTP $code")
                }
            } finally {
                conn.disconnect()
            }
        }
        throw IllegalStateException("Too many redirects")
    }

    suspend fun downloadUpdateApk(
        context: Context,
        apkUrl: String,
        expectedSha256: String? = null,
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val tempFile = File(updatesDir, "aura-tv-update.apk.tmp")
        val targetFile = File(updatesDir, "aura-tv-update.apk")
        tempFile.delete()

        try {
            var currentUrl = normalizeDownloadUrl(apkUrl)
            var activeConn: HttpURLConnection? = null
            for (redirect in 0..6) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = false
                    useCaches = false
                    setRequestProperty("User-Agent", "AuraTV-Updater/${BuildConfig.VERSION_NAME}")
                    setRequestProperty(
                        "Accept",
                        "application/vnd.android.package-archive, application/octet-stream, */*"
                    )
                }
                val code = conn.responseCode
                if (code in 300..399) {
                    val loc = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (loc.isNullOrBlank()) {
                        return@withContext Result.failure(IllegalStateException("Redirect missing Location"))
                    }
                    currentUrl = URL(URL(currentUrl), loc).toString()
                } else if (code in 200..299) {
                    activeConn = conn
                    break
                } else {
                    conn.disconnect()
                    return@withContext Result.failure(IllegalStateException("HTTP $code"))
                }
            }

            val conn = activeConn
                ?: return@withContext Result.failure(IllegalStateException("Too many redirects"))

            try {
                val totalBytes = conn.contentLengthLong.coerceAtLeast(-1L)
                val totalMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else 0f
                val digest = MessageDigest.getInstance("SHA-256")
                var downloadedBytes = 0L
                val buffer = ByteArray(16 * 1024)
                var lastEmitTime = 0L

                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                            downloadedBytes += read
                            val now = System.currentTimeMillis()
                            if (now - lastEmitTime >= 80L) {
                                lastEmitTime = now
                                val dlMb = downloadedBytes / (1024f * 1024f)
                                val progress = if (totalBytes > 0) {
                                    (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                } else -1f
                                withContext(Dispatchers.Main) {
                                    onProgress(progress, dlMb, totalMb)
                                }
                            }
                        }
                        output.flush()
                    }
                }

                val finalDlMb = downloadedBytes / (1024f * 1024f)
                withContext(Dispatchers.Main) {
                    onProgress(1f, finalDlMb, if (totalMb > 0f) totalMb else finalDlMb)
                }

                // Validate APK ZIP magic bytes ('P','K',0x03,0x04)
                if (!isValidApkFile(tempFile)) {
                    tempFile.delete()
                    return@withContext Result.failure(IllegalStateException("Downloaded file is not a valid APK package"))
                }

                if (!expectedSha256.isNullOrBlank()) {
                    val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
                    if (!actualHash.equals(expectedSha256.trim(), ignoreCase = true)) {
                        tempFile.delete()
                        return@withContext Result.failure(IllegalStateException("APK SHA-256 checksum mismatch"))
                    }
                }

                if (targetFile.exists()) targetFile.delete()
                if (!tempFile.renameTo(targetFile)) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }
                Result.success(targetFile)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            tempFile.delete()
            Result.failure(e)
        }
    }

    private fun isValidApkFile(file: File): Boolean {
        if (!file.exists() || file.length() < 1024L) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(4)
                val count = input.read(header)
                count == 4 &&
                    header[0] == 0x50.toByte() &&
                    header[1] == 0x4B.toByte() &&
                    header[2] == 0x03.toByte() &&
                    header[3] == 0x04.toByte()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun canInstallPackages(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openUnknownSourcesSettings(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val apkUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                @Suppress("DEPRECATION")
                val fallback = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = apkUri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
