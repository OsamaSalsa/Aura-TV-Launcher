package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.example.data.local.LauncherPreferences
import com.example.data.model.AppItem
import com.example.ui.common.AuroraColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AppRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val packageManager: PackageManager = context.packageManager
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val reloadTrigger = MutableStateFlow(System.currentTimeMillis())

    // In-memory cache of decoded app metadata, bitmaps, and palette colors keyed by packageName
    private val packageCache = ConcurrentHashMap<String, AppItem>()

    private val appUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            if (pkg != null) {
                packageCache.remove(pkg)
            } else {
                packageCache.clear()
            }
            triggerReload()
        }
    }

    init {
        // Seed built-in portals immediately so initial state is never empty
        seedPortalAppsIntoCache()

        // Start parallel background preload immediately upon repository creation
        repoScope.launch {
            try {
                warmUpPackageCache()
                reloadTrigger.value = System.currentTimeMillis()
            } catch (_: Exception) {}
        }

        try {
            val filter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            context.registerReceiver(appUpdateReceiver, filter)
        } catch (_: Exception) {}
    }

    fun triggerReload() {
        reloadTrigger.value = System.currentTimeMillis()
    }

    fun getInitialFastApps(): List<AppItem> {
        if (packageCache.isEmpty()) {
            seedPortalAppsIntoCache()
        }
        return packageCache.values
            .filter { it.banner != null || it.packageName.endsWith(".portal") }
            .sortedWith(
                compareBy(
                    { app ->
                        when (app.packageName) {
                            "com.minidownloader.portal" -> 0
                            "com.aurastore.portal" -> 1
                            "com.miniyube.portal" -> 2
                            else -> 3
                        }
                    },
                    { it.label.lowercase() }
                )
            )
    }

    /**
     * Flow of real installed apps, respecting saved order, hidden apps, and game categorization.
     */
    val installedAppsFlow: Flow<List<AppItem>> = combine(
        combine(
            reloadTrigger,
            preferences.appOrderFlow,
            preferences.hiddenAppsFlow,
            preferences.customGamesFlow,
            preferences.customNonGamesFlow
        ) { _, savedOrder, hiddenPackages, customGames, customNonGames ->
            withContext(Dispatchers.Default) {
                loadInstalledApps(savedOrder, hiddenPackages, customGames, customNonGames)
            }
        },
        preferences.showOnlyTvAppsFlow,
        preferences.lockedAppsFlow
    ) { apps, tvOnly, lockedPackages ->
        val filtered = if (tvOnly) {
            apps.filter { it.banner != null || it.packageName.endsWith(".portal") }
        } else {
            apps
        }
        filtered.map { it.copy(isLocked = lockedPackages.contains(it.packageName)) }
    }

    /**
     * Flow of all apps including hidden ones (used in Settings to manage hidden apps).
     */
    val allAppsFlow: Flow<List<AppItem>> = combine(
        reloadTrigger,
        preferences.hiddenAppsFlow,
        preferences.customGamesFlow,
        preferences.customNonGamesFlow,
        preferences.lockedAppsFlow
    ) { _, hiddenPackages, customGames, customNonGames, lockedPackages ->
        withContext(Dispatchers.Default) {
            loadInstalledApps(emptyList(), emptySet(), customGames, customNonGames, includeHidden = true)
                .map {
                    it.copy(
                        isHidden = hiddenPackages.contains(it.packageName),
                        isLocked = lockedPackages.contains(it.packageName)
                    )
                }
        }
    }

    private fun checkIfGame(
        resolveInfo: ResolveInfo?,
        packageName: String,
        label: String,
        customGames: Set<String>,
        customNonGames: Set<String>,
        defaultIsGame: Boolean = false
    ): Boolean {
        if (customGames.contains(packageName)) return true
        if (customNonGames.contains(packageName)) return false
        if (resolveInfo == null) return defaultIsGame

        val appInfo = resolveInfo.activityInfo.applicationInfo
        val isGameCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfo.category == android.content.pm.ApplicationInfo.CATEGORY_GAME
        } else false
        val isGameFlag = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_IS_GAME) != 0
        val isGamePkg = packageName.contains(".game", ignoreCase = true) ||
            packageName.contains(".games", ignoreCase = true) ||
            packageName.contains(".arcade", ignoreCase = true) ||
            packageName.contains(".emulator", ignoreCase = true)
        val isGameLabel = label.contains("Game", ignoreCase = true) ||
            label.contains("لعبة", ignoreCase = true)

        return isGameCategory || isGameFlag || isGamePkg || isGameLabel
    }

    private fun seedPortalAppsIntoCache() {
        val portalPackages = listOf(
            Triple("com.minidownloader.portal", "Mini Downloader", false),
            Triple("com.miniyube.portal", "MiniTube", false),
            Triple("com.crazygames.portal", "CrazyGames", true),
            Triple("com.poki.portal", "Poki", true),
            Triple("com.playhop.portal", "Playhop", true),
            Triple("com.easygames.portal", "EasyGames", true),
            Triple("com.aurastore.portal", "Aura Store", false)
        )
        for ((pkg, label, defaultIsGame) in portalPackages) {
            if (!packageCache.containsKey(pkg)) {
                packageCache[pkg] = AppItem(
                    packageName = pkg,
                    activityName = "${label}Screen",
                    label = label,
                    icon = null,
                    banner = null,
                    iconBitmap = null,
                    bannerBitmap = null,
                    dominantColor = when (pkg) {
                        "com.minidownloader.portal" -> 0xFFE91E63.toInt()
                        "com.aurastore.portal" -> 0xFF8B5CF6.toInt()
                        "com.miniyube.portal" -> 0xFFEF4444.toInt()
                        "com.crazygames.portal" -> 0xFFFFD700.toInt()
                        "com.poki.portal" -> 0xFFFBBF24.toInt()
                        "com.playhop.portal" -> 0xFFD97706.toInt()
                        else -> 0xFFFBBF24.toInt()
                    },
                    auroraColors = AuroraColors(),
                    isHidden = false,
                    isGame = defaultIsGame
                )
            }
        }
    }

    private suspend fun warmUpPackageCache() = coroutineScope {
        val ownPackage = context.packageName
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val leanbackApps = packageManager.queryIntentActivities(leanbackIntent, 0)
        val launcherApps = packageManager.queryIntentActivities(launcherIntent, 0)

        val uniqueResolves = LinkedHashMap<String, ResolveInfo>()
        for (info in leanbackApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != ownPackage) uniqueResolves[pkg] = info
        }
        for (info in launcherApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != ownPackage && !uniqueResolves.containsKey(pkg)) {
                uniqueResolves[pkg] = info
            }
        }

        // Remove uninstalled packages from cache (keeping portals)
        val installedSet = uniqueResolves.keys
        for (cachedPkg in packageCache.keys().toList()) {
            if (!cachedPkg.endsWith(".portal") && !installedSet.contains(cachedPkg)) {
                packageCache.remove(cachedPkg)
            }
        }

        // Load any uncached packages in parallel across CPU cores
        uniqueResolves.entries
            .filter { !packageCache.containsKey(it.key) }
            .map { (pkg, resolveInfo) ->
                async(Dispatchers.Default) {
                    val item = buildAppItem(pkg, resolveInfo)
                    packageCache[pkg] = item
                }
            }
            .awaitAll()
    }

    private fun buildAppItem(pkg: String, resolveInfo: ResolveInfo): AppItem {
        val label = resolveInfo.loadLabel(packageManager)?.toString() ?: pkg
        val banner = resolveInfo.activityInfo.loadBanner(packageManager)
            ?: resolveInfo.activityInfo.applicationInfo.loadBanner(packageManager)
        val icon = resolveInfo.loadIcon(packageManager)

        val bannerBmp = banner?.let { drawableToBitmap(it, maxWidth = 320, maxHeight = 180) }
        val iconBmp = icon?.let { drawableToBitmap(it, maxWidth = 96, maxHeight = 96) }

        val sampleBmp = bannerBmp ?: iconBmp
        val (dominantColor, aurora) = sampleBmp?.let { extractColorAndAuroraFast(it) }
            ?: (0xFFFBBF24.toInt() to AuroraColors())

        val isGame = checkIfGame(resolveInfo, pkg, label, emptySet(), emptySet())

        return AppItem(
            packageName = pkg,
            activityName = resolveInfo.activityInfo.name,
            label = label,
            icon = icon,
            banner = banner,
            iconBitmap = iconBmp?.asImageBitmap(),
            bannerBitmap = bannerBmp?.asImageBitmap(),
            dominantColor = dominantColor,
            auroraColors = aurora,
            isHidden = false,
            isGame = isGame
        )
    }

    private suspend fun loadInstalledApps(
        savedOrder: List<String>,
        hiddenPackages: Set<String>,
        customGames: Set<String> = emptySet(),
        customNonGames: Set<String> = emptySet(),
        includeHidden: Boolean = false
    ): List<AppItem> {
        seedPortalAppsIntoCache()
        warmUpPackageCache()

        val portalDefaultGame = mapOf(
            "com.minidownloader.portal" to false,
            "com.miniyube.portal" to false,
            "com.crazygames.portal" to true,
            "com.poki.portal" to true,
            "com.playhop.portal" to true,
            "com.easygames.portal" to true,
            "com.aurastore.portal" to false
        )

        val allList = packageCache.values.mapNotNull { cached ->
            val pkg = cached.packageName
            val isHidden = hiddenPackages.contains(pkg)
            if (!includeHidden && isHidden) return@mapNotNull null

            val isGame = when {
                customGames.contains(pkg) -> true
                customNonGames.contains(pkg) -> false
                portalDefaultGame.containsKey(pkg) -> portalDefaultGame[pkg] ?: false
                else -> cached.isGame
            }

            cached.copy(
                isHidden = isHidden,
                isGame = isGame
            )
        }

        // Apply saved order:
        if (savedOrder.isEmpty()) {
            return allList.sortedWith(
                compareBy(
                    { app ->
                        val isTvApp = app.banner != null || app.packageName.endsWith(".portal")
                        when {
                            app.packageName == "com.minidownloader.portal" -> 0
                            app.packageName == "com.aurastore.portal" -> 1
                            app.packageName == "com.miniyube.portal" -> 2
                            isTvApp -> 3
                            else -> 4
                        }
                    },
                    { it.label.lowercase() }
                )
            )
        }

        val orderMap = savedOrder.withIndex().associate { it.value to it.index }
        return allList.sortedWith(
            compareBy(
                {
                    if (it.packageName == "com.minidownloader.portal" || it.packageName == "com.aurastore.portal" || it.packageName == "com.miniyube.portal") {
                        orderMap[it.packageName] ?: (if (it.packageName == "com.minidownloader.portal") -3 else if (it.packageName == "com.aurastore.portal") -2 else -1)
                    } else {
                        orderMap[it.packageName] ?: Int.MAX_VALUE
                    }
                },
                { it.label.lowercase() }
            )
        )
    }

    /**
     * Single-pass fast Palette extraction (samples 16x16 = 256 pixels max) to compute both
     * dominantColor and AuroraColors in <1ms per icon instead of two full-bitmap scans.
     */
    private fun extractColorAndAuroraFast(bitmap: Bitmap): Pair<Int, AuroraColors> {
        return try {
            val palette = Palette.from(bitmap)
                .resizeBitmapArea(256)
                .maximumColorCount(16)
                .generate()

            val rawColor = palette.getVibrantColor(
                palette.getLightVibrantColor(
                    palette.getDominantColor(
                        palette.getMutedColor(0xFFFBBF24.toInt())
                    )
                )
            )

            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(rawColor, hsv)
            if (hsv[2] < 0.55f) hsv[2] = 0.70f
            if (hsv[1] < 0.40f) hsv[1] = 0.65f
            val dominantColor = android.graphics.Color.HSVToColor(hsv)

            val src = palette.vibrantSwatch?.rgb
                ?: palette.dominantSwatch?.rgb
                ?: palette.lightVibrantSwatch?.rgb
                ?: palette.darkVibrantSwatch?.rgb
                ?: palette.mutedSwatch?.rgb
                ?: return dominantColor to AuroraColors()

            fun shift(h: Float, l: Float? = null, s: Float? = null): Color {
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(src, hsl)
                hsl[0] = (hsl[0] + h + 360f) % 360f
                l?.let { hsl[2] = it }
                s?.let { hsl[1] = it }
                return Color(ColorUtils.HSLToColor(hsl))
            }

            val aurora = AuroraColors(
                glow = shift(0f, l = 0.58f, s = 0.95f),
                mid = shift(-25f, l = 0.48f, s = 0.85f),
                accent = shift(70f, l = 0.40f, s = 0.75f),
                deep = shift(-10f, l = 0.22f, s = 0.80f),
                base = shift(0f, l = 0.02f, s = 0.40f),
                teal = shift(25f, l = 0.40f, s = 0.75f)
            )

            dominantColor to aurora
        } catch (_: Exception) {
            0xFFFBBF24.toInt() to AuroraColors()
        }
    }

    private fun drawableToBitmap(drawable: Drawable, maxWidth: Int = 128, maxHeight: Int = 128): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(maxWidth) else 96
        val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(maxHeight) else 96
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun launchApp(app: AppItem) {
        try {
            val intent = packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                ?: packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveNewOrder(apps: List<AppItem>) {
        val packageNames = apps.map { it.packageName }
        preferences.saveAppOrder(packageNames)
        triggerReload()
    }

    suspend fun hideApp(packageName: String) {
        preferences.hideApp(packageName)
        triggerReload()
    }

    suspend fun unhideApp(packageName: String) {
        preferences.unhideApp(packageName)
        triggerReload()
    }

    suspend fun unhideAll() {
        preferences.unhideAllApps()
        triggerReload()
    }

    suspend fun resetOrder() {
        preferences.resetAppOrder()
        triggerReload()
    }
}
