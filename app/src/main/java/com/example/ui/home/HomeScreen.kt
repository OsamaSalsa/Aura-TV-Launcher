package com.example.ui.home

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.common.BookmarkTileImage
import com.example.util.FilePermissionHelper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.AppItem
import com.example.data.model.IptvChannel
import com.example.ui.common.DynamicBackground
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvActionButton
import com.example.ui.common.TvTopBar
import com.example.ui.common.GlassSheenOverlay
import com.example.ui.common.drawableToBitmapSafe
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvDeepBlue
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    voiceSearchTriggerTick: Int = 0,
    consumedVoiceSearchTick: Int = 0,
    onVoiceSearchConsumed: () -> Unit = {},
    userActivityFlow: kotlinx.coroutines.flow.SharedFlow<Long>? = null,
    isPlaybackActive: Boolean = false,
    onOpenIptv: (channelId: Long?) -> Unit,
    onOpenYouTube: () -> Unit,
    onOpenCrazyGames: () -> Unit,
    onOpenPoki: () -> Unit,
    onOpenPlayhop: () -> Unit,
    onOpenEasyGames: () -> Unit,
    onOpenAuraStore: () -> Unit,
    onOpenMiniDownloader: () -> Unit,
    onOpenWebBookmark: (title: String, url: String) -> Unit = { _, _ -> },
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
    }
    val isRtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
    val installedApps by viewModel.installedApps.collectAsState()
    val favoriteChannels by viewModel.favoriteChannels.collectAsState()
    val isDynamicBg by viewModel.isDynamicBgEnabled.collectAsState()
    val wallpaperMode by viewModel.wallpaperMode.collectAsState()
    val customWallpaperUrl by viewModel.customWallpaperUrl.collectAsState()
    val headerControlMode by viewModel.headerControlMode.collectAsState()
    val isClock24h by viewModel.isClock24h.collectAsState()
    val isClockSecondsEnabled by viewModel.isClockSecondsEnabled.collectAsState()
    val clockSize by viewModel.clockSize.collectAsState()
    val dateFormatStyle by viewModel.dateFormatStyle.collectAsState()
    val greetingMode by viewModel.greetingMode.collectAsState()
    val customGreetingText by viewModel.customGreetingText.collectAsState()
    val appsPerRow by viewModel.appsPerRow.collectAsState()
    val appsTopOffset by viewModel.appsTopOffset.collectAsState()
    val glassOpacity by viewModel.glassOpacity.collectAsState()
    val selectedAppForMenu by viewModel.selectedAppForMenu.collectAsState()
    val reorderState by viewModel.reorderState.collectAsState()
    val editingMode by viewModel.editingMode.collectAsState()
    val showLauncherPrompt by viewModel.showDefaultLauncherPrompt.collectAsState()
    val channelReorderState by viewModel.channelReorderState.collectAsState()
    val selectedChannelForMenu by viewModel.selectedChannelForMenu.collectAsState()
    val allChannels by viewModel.allChannels.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()
    val screensaverTimeout by viewModel.screensaverTimeout.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val screensaverWallpaper by viewModel.screensaverWallpaper.collectAsState()
    val screensaverVideo by viewModel.screensaverVideo.collectAsState()
    val showRecentApps by viewModel.showRecentApps.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()

    var showUniversalSearch by rememberSaveable { mutableStateOf(false) }
    var autoStartVoiceInSearch by rememberSaveable { mutableStateOf(false) }
    var localVoiceTick by rememberSaveable { mutableStateOf(0) }
    var showControlCenter by rememberSaveable { mutableStateOf(false) }
    var showAmbientMode by rememberSaveable { mutableStateOf(false) }
    var pendingLockedAppForLaunch by remember { mutableStateOf<AppItem?>(null) }
    var pendingLockedAppForToggle by remember { mutableStateOf<AppItem?>(null) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isActivityResumed by remember { mutableStateOf(true) }

    LaunchedEffect(userActivityFlow) {
        userActivityFlow?.collect { ts ->
            lastInteractionTime = ts
        }
    }

    LaunchedEffect(isPlaybackActive) {
        if (isPlaybackActive) {
            lastInteractionTime = System.currentTimeMillis()
            if (showAmbientMode) {
                showAmbientMode = false
            }
        }
    }

    // In-app inactivity sleep / screensaver timer
    val timeoutMinutes = remember(screensaverTimeout) {
        if (screensaverTimeout == "never") 0 else (screensaverTimeout.toIntOrNull() ?: 5)
    }

    LaunchedEffect(screensaverTimeout, timeoutMinutes, showAmbientMode, lastInteractionTime, isPlaybackActive, isActivityResumed) {
        if (screensaverTimeout == "never" || timeoutMinutes <= 0) {
            showAmbientMode = false
        } else if (!showAmbientMode) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                val isMediaOrVideoPlaying = isPlaybackActive || (audioManager?.isMusicActive == true) || !isActivityResumed
                if (isMediaOrVideoPlaying) {
                    lastInteractionTime = System.currentTimeMillis()
                } else {
                    val elapsed = System.currentTimeMillis() - lastInteractionTime
                    val limitMs = timeoutMinutes * 60 * 1000L
                    if (elapsed >= limitMs && !showAmbientMode && !showControlCenter && !showUniversalSearch && pendingLockedAppForLaunch == null) {
                        showAmbientMode = true
                    }
                }
            }
        }
    }

    LaunchedEffect(voiceSearchTriggerTick, consumedVoiceSearchTick) {
        if (voiceSearchTriggerTick > consumedVoiceSearchTick) {
            onVoiceSearchConsumed()
            showAmbientMode = false
            showControlCenter = false
            viewModel.dismissAppMenu()
            viewModel.dismissChannelMenu()
            pendingLockedAppForLaunch = null
            pendingLockedAppForToggle = null
            autoStartVoiceInSearch = true
            localVoiceTick++
            showUniversalSearch = true
        }
    }

    fun executeAppOpen(app: AppItem) {
        viewModel.recordAppLaunch(app)
        when {
            app.bookmarkUrl != null -> onOpenWebBookmark(app.label, app.bookmarkUrl)
            app.packageName == "com.minidownloader.portal" -> onOpenMiniDownloader()
            app.packageName == "com.aurastore.portal" -> onOpenAuraStore()
            app.packageName == "com.miniyube.portal" -> onOpenYouTube()
            app.packageName == "com.crazygames.portal" -> onOpenCrazyGames()
            app.packageName == "com.poki.portal" -> onOpenPoki()
            app.packageName == "com.playhop.portal" -> onOpenPlayhop()
            app.packageName == "com.easygames.portal" -> onOpenEasyGames()
            else -> viewModel.launchApp(app)
        }
    }

    fun handleAppLaunchRequest(app: AppItem) {
        if (app.isLocked) {
            pendingLockedAppForLaunch = app
        } else {
            executeAppOpen(app)
        }
    }

    var isDefaultLauncherChecked by rememberSaveable { mutableStateOf(false) }
    var isDefaultLauncherCached by rememberSaveable { mutableStateOf(true) }
    var showStoragePermissionPrompt by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(950L)
        val isDef = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            viewModel.isDefaultLauncher(context)
        }
        val hasStorage = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            FilePermissionHelper.hasStoragePermission(context)
        }
        isDefaultLauncherCached = isDef
        isDefaultLauncherChecked = true
        showStoragePermissionPrompt = !hasStorage
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isActivityResumed = true
                    lastInteractionTime = System.currentTimeMillis()
                    if (FilePermissionHelper.hasStoragePermission(context)) {
                        showStoragePermissionPrompt = false
                    }
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    isActivityResumed = false
                    lastInteractionTime = System.currentTimeMillis()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.onDefaultLauncherPromptDismissed()
    }

    val currentLayoutDir = androidx.compose.ui.platform.LocalLayoutDirection.current

    if (isDefaultLauncherChecked && showLauncherPrompt && !isDefaultLauncherCached) {
        Dialog(onDismissRequest = { viewModel.onDefaultLauncherPromptDismissed() }) {
            CompositionLocalProvider(
                LocalContext provides context,
                androidx.compose.ui.platform.LocalConfiguration provides context.resources.configuration,
                androidx.compose.ui.platform.LocalLayoutDirection provides currentLayoutDir
            ) {
                Surface(
                shape = RoundedCornerShape(24.dp),
                color = TvMidnightBackground.copy(alpha = 0.98f),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .width(420.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.launcher_prompt_title),
                        color = TvTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.launcher_prompt_desc),
                        color = TvTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    TvFocusableCard(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 29) {
                                val rm = context.getSystemService(RoleManager::class.java)
                                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                                    roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                                } else {
                                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    viewModel.onDefaultLauncherPromptDismissed()
                                }
                            } else {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                                viewModel.onDefaultLauncherPromptDismissed()
                            }
                        },
                        scaleUpRatio = 1.05f,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_set_default),
                                color = if (isFocused) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TvFocusableCard(
                        onClick = { viewModel.onDefaultLauncherPromptDismissed() },
                        scaleUpRatio = 1.04f,
                        elevation = 0.dp,
                        cardBackgroundColor = Color.Transparent,
                        useDefaultBorder = false,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFocused) Color.White.copy(alpha = 0.16f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_later),
                                color = if (isFocused) TvAccentGold else TvTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

    if (isDefaultLauncherChecked && (!showLauncherPrompt || isDefaultLauncherCached) && showStoragePermissionPrompt) {
        Dialog(onDismissRequest = { showStoragePermissionPrompt = false }) {
            CompositionLocalProvider(
                LocalContext provides context,
                androidx.compose.ui.platform.LocalConfiguration provides context.resources.configuration,
                androidx.compose.ui.platform.LocalLayoutDirection provides currentLayoutDir
            ) {
                Surface(
                shape = RoundedCornerShape(24.dp),
                color = TvMidnightBackground.copy(alpha = 0.98f),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .width(440.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.file_permission_dialog_title),
                        color = TvTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.file_permission_dialog_desc),
                        color = TvTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    TvFocusableCard(
                        onClick = {
                            FilePermissionHelper.requestStoragePermission(context)
                        },
                        scaleUpRatio = 1.05f,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_grant_permission),
                                color = if (isFocused) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TvFocusableCard(
                        onClick = { showStoragePermissionPrompt = false },
                        scaleUpRatio = 1.04f,
                        elevation = 0.dp,
                        cardBackgroundColor = Color.Transparent,
                        useDefaultBorder = false,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFocused) Color.White.copy(alpha = 0.16f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_later),
                                color = if (isFocused) TvAccentGold else TvTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

    val displayApps = reorderState?.items ?: installedApps
    val activeReorderPkg = reorderState?.selectedPackage
    
    val displayChannels = channelReorderState?.items ?: favoriteChannels
    val activeReorderChannelId = channelReorderState?.selectedChannelId
    val isReorderingActive = reorderState != null || channelReorderState != null
    
    val focusRequesters = remember { mutableMapOf<String, androidx.compose.ui.focus.FocusRequester>() }
    val channelFocusRequesters = remember { mutableMapOf<Long, androidx.compose.ui.focus.FocusRequester>() }
    val channelsRowState = androidx.compose.foundation.lazy.rememberLazyListState()

    val clampedAppsPerRow = appsPerRow.coerceIn(4, 7)
    var selectedCategoryFilter by rememberSaveable { mutableStateOf("all") }
    val effectiveFilter = if (reorderState != null) "all" else selectedCategoryFilter
    val nonGameApps = remember(displayApps, effectiveFilter) {
        when (effectiveFilter) {
            "games" -> emptyList()
            "apps" -> displayApps.filter { !it.isGame && it.bookmarkUrl == null }
            "web" -> displayApps.filter { !it.isGame && (it.bookmarkUrl != null || it.packageName.endsWith(".portal")) }
            else -> displayApps.filter { !it.isGame }
        }
    }
    val gameApps = remember(displayApps, effectiveFilter) {
        when (effectiveFilter) {
            "apps" -> emptyList()
            "web" -> displayApps.filter { it.isGame && (it.bookmarkUrl != null || it.packageName.endsWith(".portal")) }
            else -> displayApps.filter { it.isGame }
        }
    }
    val nonGameRows = remember(nonGameApps, clampedAppsPerRow) { nonGameApps.chunked(clampedAppsPerRow) }
    val gameRows = remember(gameApps, clampedAppsPerRow) { gameApps.chunked(clampedAppsPerRow) }

    val firstItemFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val topBarFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val startupParkFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    var isStartupParked by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val density = androidx.compose.ui.platform.LocalDensity.current

    LaunchedEffect(activeReorderPkg, activeReorderChannelId, displayApps, displayChannels) {
        activeReorderPkg?.let { pkg ->
            isStartupParked = false
            // Compute exact LazyColumn item index containing the moved app
            var cursor = 1 // index 0 is top_anchor_spacer
            if (favoriteChannels.isNotEmpty()) cursor++
            if (showRecentApps && recentApps.isNotEmpty() && reorderState == null) cursor++
            var targetListIndex: Int? = null
            if (nonGameApps.isNotEmpty()) {
                cursor++ // apps_header
                val rowIdx = nonGameRows.indexOfFirst { row -> row.any { it.packageName == pkg } }
                if (rowIdx != -1) {
                    targetListIndex = cursor + rowIdx
                }
                cursor += nonGameRows.size
            }
            if (targetListIndex == null && gameApps.isNotEmpty()) {
                cursor++ // games_header
                val rowIdx = gameRows.indexOfFirst { row -> row.any { it.packageName == pkg } }
                if (rowIdx != -1) {
                    targetListIndex = cursor + rowIdx
                }
            }

            if (targetListIndex != null) {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val targetVisible = visibleItems.find { it.index == targetListIndex }
                val viewportEnd = listState.layoutInfo.viewportEndOffset
                val topClearancePx = with(density) { 96.dp.roundToPx() }
                val bottomClearancePx = with(density) { 56.dp.roundToPx() }
                if (targetVisible != null) {
                    val dy = when {
                        targetVisible.offset < topClearancePx -> (targetVisible.offset - topClearancePx).toFloat()
                        targetVisible.offset + targetVisible.size > viewportEnd - bottomClearancePx ->
                            (targetVisible.offset + targetVisible.size - (viewportEnd - bottomClearancePx)).toFloat()
                        else -> 0f
                    }
                    if (dy != 0f) {
                        try {
                            listState.animateScrollBy(dy)
                        } catch (_: Exception) {}
                    }
                } else {
                    try {
                        listState.scrollToItem((targetListIndex - 1).coerceAtLeast(1), 0)
                    } catch (_: Exception) {}
                }
            }

            kotlinx.coroutines.yield()
            val focused = try {
                focusRequesters[pkg]?.requestFocus()
                true
            } catch (_: Exception) {
                false
            }
            if (!focused) {
                kotlinx.coroutines.delay(45)
                try {
                    focusRequesters[pkg]?.requestFocus()
                } catch (_: Exception) {}
            }
        }
        activeReorderChannelId?.let { id ->
            isStartupParked = false
            val idx = displayChannels.indexOfFirst { it.id == id }
            if (idx != -1) {
                try {
                    channelsRowState.scrollToItem((idx - 1).coerceAtLeast(0))
                } catch (_: Exception) {}
                kotlinx.coroutines.delay(40)
                try {
                    channelFocusRequesters[id]?.requestFocus()
                } catch (_: Exception) {}
            }
        }
    }
    val shrinkThresholdPx = remember(density) { with(density) { 240.dp.toPx() } }
    // Shrink the clock as soon as the top of the first apps/channels section reaches <= 240dp from the top of the screen
    val isScrolled by remember(shrinkThresholdPx, appsTopOffset) {
        androidx.compose.runtime.derivedStateOf {
            if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                false
            } else if (listState.firstVisibleItemIndex > 0) {
                true
            } else {
                val firstContentItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index >= 1 }
                if (firstContentItem != null) {
                    firstContentItem.offset <= shrinkThresholdPx
                } else {
                    val fallbackScrollPx = with(density) { (appsTopOffset - 240).coerceAtLeast(10).dp.toPx() }
                    listState.firstVisibleItemScrollOffset >= fallbackScrollPx
                }
            }
        }
    }

    fun scrollHomeToBottomIdle() {
        coroutineScope.launch {
            try {
                listState.animateScrollToItem(0, 0)
            } catch (_: Exception) {}
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    val gradualBringIntoViewSpec = remember(density) {
        object : BringIntoViewSpec {
            private val topSafePx = with(density) { 92.dp.toPx() }
            private val bottomSafePx = with(density) { 28.dp.toPx() }

            override fun calculateScrollDistance(
                offset: Float,
                size: Float,
                containerSize: Float
            ): Float {
                val maxTrailing = (containerSize - bottomSafePx).coerceAtLeast(size)
                val minLeading = topSafePx.coerceAtMost(maxTrailing - size).coerceAtLeast(0f)
                val trailingEdge = offset + size
                return when {
                    offset >= minLeading && trailingEdge <= maxTrailing -> 0f
                    offset < minLeading && trailingEdge > maxTrailing -> 0f
                    offset < minLeading -> offset - minLeading
                    else -> trailingEdge - maxTrailing
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    val horizontalBringIntoViewSpec = remember(density) {
        object : BringIntoViewSpec {
            private val edgePaddingPx = with(density) { 48.dp.toPx() }

            override fun calculateScrollDistance(
                offset: Float,
                size: Float,
                containerSize: Float
            ): Float {
                val maxTrailing = (containerSize - edgePaddingPx).coerceAtLeast(size)
                val minLeading = edgePaddingPx.coerceAtMost(maxTrailing - size).coerceAtLeast(0f)
                val trailingEdge = offset + size
                return when {
                    offset >= minLeading && trailingEdge <= maxTrailing -> 0f
                    offset < minLeading && trailingEdge > maxTrailing -> 0f
                    offset < minLeading -> offset - minLeading
                    else -> trailingEdge - maxTrailing
                }
            }
        }
    }

    fun focusFirstContentItem() {
        isStartupParked = false
        coroutineScope.launch {
            try {
                if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                    listState.scrollToItem(0, 0)
                    kotlinx.coroutines.delay(45)
                }
                if (favoriteChannels.isNotEmpty()) {
                    channelsRowState.scrollToItem(0)
                    kotlinx.coroutines.delay(35)
                }
                try {
                    firstItemFocusRequester.requestFocus()
                } catch (_: Exception) {
                    val nudgePx = with(density) { 140.dp.roundToPx() }
                    try {
                        listState.animateScrollToItem(0, nudgePx)
                        kotlinx.coroutines.delay(50)
                        firstItemFocusRequester.requestFocus()
                    } catch (_: Exception) {
                        val firstChannelId = displayChannels.firstOrNull()?.id
                        val firstAppPkg = displayApps.firstOrNull()?.packageName
                        if (firstChannelId != null && channelFocusRequesters.containsKey(firstChannelId)) {
                            channelFocusRequesters[firstChannelId]?.requestFocus()
                        } else if (firstAppPkg != null && focusRequesters.containsKey(firstAppPkg)) {
                            focusRequesters[firstAppPkg]?.requestFocus()
                        } else {
                            focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
                        }
                    }
                }
            } catch (_: Exception) {
                try {
                    focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
                } catch (_: Exception) {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IsolatedHomeBackground(
            viewModel = viewModel,
            isDynamicBg = isDynamicBg,
            wallpaperMode = wallpaperMode,
            customWallpaperUrl = customWallpaperUrl
        )

        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->
                    val native = keyEvent.nativeKeyEvent
                    if (native.keyCode == android.view.KeyEvent.KEYCODE_SETTINGS) {
                        if (native.action == android.view.KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                            isStartupParked = false
                            if (headerControlMode == "direct_settings") {
                                onOpenSettings()
                            } else {
                                showControlCenter = true
                            }
                        }
                        return@onKeyEvent true
                    } else if (com.example.ui.common.isRemoteMenuKey(native.keyCode)) {
                        if (native.action == android.view.KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                            focusFirstContentItem()
                        }
                        return@onKeyEvent true
                    } else if (native.keyCode == android.view.KeyEvent.KEYCODE_SEARCH ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_ASSIST ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_VOICE_ASSIST
                    ) {
                        if (native.action == android.view.KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                            isStartupParked = false
                            showAmbientMode = false
                            showControlCenter = false
                            autoStartVoiceInSearch = true
                            localVoiceTick++
                            showUniversalSearch = true
                        }
                        return@onKeyEvent true
                    }
                    false
                }
        ) {
            val totalWidth = maxWidth
            val horizontalPadding = 48.dp * 2
            val itemSpacing = 18.dp
            val cardHeight = (totalWidth - horizontalPadding - (itemSpacing * (appsPerRow - 1))) / appsPerRow * 0.5625f

            // Apply the user's selected starting offset (900, 700, 500, 300, 100 dp) directly via the top anchor spacer
            val topSpacerHeight = (appsTopOffset - 14).coerceAtLeast(0).dp

            // Invisible parked focus anchor at startup:
            // Keeps the cursor hidden ("لا يقف على أي مكان") at launch while routing the first D-Pad press
            // either Down to the bottom apps/channels row or Up/Left/Right/OK to the Top Bar.
            Box(
                modifier = Modifier
                    .size(1.dp)
                    .align(Alignment.TopCenter)
                    .focusRequester(startupParkFocusRequester)
                    .focusable(enabled = isStartupParked && !isReorderingActive)
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.DirectionDown -> {
                                    focusFirstContentItem()
                                    true
                                }
                                Key.DirectionUp,
                                Key.DirectionLeft,
                                Key.DirectionRight,
                                Key.DirectionCenter,
                                Key.Enter,
                                Key.NumPadEnter -> {
                                    isStartupParked = false
                                    try {
                                        topBarFocusRequester.requestFocus()
                                    } catch (_: Exception) {}
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    }
            )

            LaunchedEffect(
                isStartupParked,
                appsTopOffset,
                favoriteChannels.isNotEmpty(),
                showRecentApps,
                recentApps.isNotEmpty(),
                showLauncherPrompt,
                showStoragePermissionPrompt
            ) {
                if (isStartupParked && !showLauncherPrompt && !showStoragePermissionPrompt) {
                    if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                        try {
                            listState.scrollToItem(0, 0)
                        } catch (_: Exception) {}
                    }
                    kotlinx.coroutines.delay(50)
                    if (isStartupParked) {
                        try {
                            startupParkFocusRequester.requestFocus()
                        } catch (_: Exception) {}
                    }
                }
            }

            @OptIn(ExperimentalFoundationApi::class)
            CompositionLocalProvider(LocalBringIntoViewSpec provides gradualBringIntoViewSpec) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(top = 0.dp, bottom = 36.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                    // Permanent index-0 anchor spacer so async loading of Favorite Channels or Recent Apps
                    // never shifts LazyColumn's firstVisibleItemIndex or shrinks the top clock.
                    item(key = "top_anchor_spacer") {
                        Spacer(modifier = Modifier.height(topSpacerHeight))
                    }

                    if (favoriteChannels.isNotEmpty()) {
                        item(key = "fav_channels_section") {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Star, null, tint = TvAccentGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.title_favorite_channels), color = TvTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }

                                val currentChannelReorder = channelReorderState
                                @OptIn(ExperimentalFoundationApi::class)
                                CompositionLocalProvider(LocalBringIntoViewSpec provides horizontalBringIntoViewSpec) {
                                    LazyRow(
                                        state = channelsRowState,
                                        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        itemsIndexed(displayChannels, key = { _, channel -> channel.id }) { index, channel ->
                                            val isBeingReordered = channel.id == activeReorderChannelId
                                            val defaultFr = channelFocusRequesters.getOrPut(channel.id) { androidx.compose.ui.focus.FocusRequester() }
                                            
                                            FavoriteChannelCard(
                                                channel = channel,
                                                cardSize = cardHeight.coerceAtLeast(100.dp),
                                                opacityPercent = glassOpacity,
                                                isHighlighted = isBeingReordered,
                                                focusRequester = defaultFr,
                                                modifier = if (index == 0 && activeReorderChannelId == null) {
                                                    Modifier.focusRequester(firstItemFocusRequester)
                                                } else {
                                                    Modifier
                                                },
                                                onClick = { 
                                                    if (currentChannelReorder != null) {
                                                        viewModel.showChannelMenu(channel)
                                                    } else if (editingMode != EditingMode.NONE) {
                                                        viewModel.startChannelReorder(channel)
                                                    } else {
                                                        onOpenIptv(channel.id)
                                                    }
                                                },
                                                onLongClick = {
                                                    viewModel.showChannelMenu(channel)
                                                },
                                                onFocused = {
                                                    isStartupParked = false
                                                    viewModel.onNonAppFocused()
                                                },
                                                onMoveLeft = { if (isRtl) viewModel.moveChannelRight() else viewModel.moveChannelLeft() },
                                                onMoveRight = { if (isRtl) viewModel.moveChannelLeft() else viewModel.moveChannelRight() },
                                                onRequestFocusUp = if (!isReorderingActive) {
                                                    {
                                                        scrollHomeToBottomIdle()
                                                        try {
                                                            topBarFocusRequester.requestFocus()
                                                        } catch (_: Exception) {}
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val isRecentFirstSection = favoriteChannels.isEmpty() && showRecentApps && recentApps.isNotEmpty() && reorderState == null
                    if (showRecentApps && recentApps.isNotEmpty() && reorderState == null) {
                        item(key = "recent_apps_section") {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.History, null, tint = TvAccentGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.home_recent_apps), color = TvTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }

                                @OptIn(ExperimentalFoundationApi::class)
                                CompositionLocalProvider(LocalBringIntoViewSpec provides horizontalBringIntoViewSpec) {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        itemsIndexed(recentApps, key = { _, app -> "recent_${app.packageName}" }) { idx, app ->
                                            val recentFr = if (isRecentFirstSection && idx == 0) firstItemFocusRequester else remember { androidx.compose.ui.focus.FocusRequester() }
                                            AppItemCard(
                                                app = app,
                                                isHighlighted = false,
                                                focusRequester = recentFr,
                                                onRequestFocusUp = if (isRecentFirstSection) {
                                                    {
                                                        scrollHomeToBottomIdle()
                                                        try {
                                                            topBarFocusRequester.requestFocus()
                                                        } catch (_: Exception) {}
                                                    }
                                                } else null,
                                                opacityPercent = glassOpacity,
                                                modifier = Modifier.width((cardHeight.coerceAtLeast(100.dp)) * 1.77f),
                                                onClick = { handleAppLaunchRequest(app) },
                                                onLongClick = { viewModel.showAppMenu(app) },
                                                onFocused = {
                                                    isStartupParked = false
                                                    viewModel.onAppFocused(app)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val isAppsFirstSection = favoriteChannels.isEmpty() && !isRecentFirstSection
                    if (displayApps.isNotEmpty()) {
                        item(key = "apps_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Apps, null, tint = TvAccentGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.title_apps), color = TvTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    if (reorderState != null && reorderState?.items?.find { it.packageName == activeReorderPkg }?.isGame == false) {
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(stringResource(R.string.reorder_mode_hint), color = TvAccentGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (reorderState == null) {
                                    val filterOptions = listOf(
                                        "all" to stringResource(R.string.iptv_all_categories),
                                        "apps" to stringResource(R.string.title_apps),
                                        "games" to stringResource(R.string.title_games),
                                        "web" to "Web"
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        filterOptions.forEach { (key, label) ->
                                            val isSelected = selectedCategoryFilter == key
                                            TvFocusableCard(
                                                onClick = { selectedCategoryFilter = key },
                                                scaleUpRatio = 1.05f,
                                                elevation = 0.dp,
                                                cardBackgroundColor = Color.Transparent,
                                                useDefaultBorder = false,
                                                shape = RoundedCornerShape(16.dp)
                                            ) { isFocused ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(
                                                            when {
                                                                isFocused -> TvAccentGold
                                                                isSelected -> TvAccentGold.copy(alpha = 0.22f)
                                                                else -> Color.White.copy(alpha = 0.08f)
                                                            }
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isFocused || isSelected) TvAccentGold else Color.White.copy(alpha = 0.14f),
                                                            shape = RoundedCornerShape(16.dp)
                                                        )
                                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isFocused) Color.Black else if (isSelected) TvAccentGold else TvTextSecondary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (nonGameApps.isNotEmpty()) {

                        itemsIndexed(
                            items = nonGameRows,
                            key = { rowIndex, _ -> "app_row_$rowIndex" }
                        ) { rowIndex, rowApps ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp),
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                rowApps.forEachIndexed { colIndex, app ->
                                    androidx.compose.runtime.key(app.packageName) {
                                        val isBeingReordered = app.packageName == activeReorderPkg
                                        val defaultFr = focusRequesters.getOrPut(app.packageName) { androidx.compose.ui.focus.FocusRequester() }
                                        val isFirstAppItem = isAppsFirstSection && rowIndex == 0 && colIndex == 0
                                        AppItemCard(
                                            app = app,
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(if (isFirstAppItem) Modifier.focusRequester(firstItemFocusRequester) else Modifier),
                                            isHighlighted = isBeingReordered,
                                            focusRequester = defaultFr,
                                            onRequestFocusUp = if (isAppsFirstSection && rowIndex == 0 && !isReorderingActive) {
                                                {
                                                    scrollHomeToBottomIdle()
                                                    try {
                                                        topBarFocusRequester.requestFocus()
                                                    } catch (_: Exception) {}
                                                }
                                            } else null,
                                            opacityPercent = glassOpacity,
                                            onClick = { 
                                                when (editingMode) {
                                                    EditingMode.MOVE -> viewModel.showAppMenu(app)
                                                    EditingMode.DELETE -> {
                                                        if (!app.packageName.endsWith(".portal")) {
                                                            viewModel.uninstallApp(context, app)
                                                        }
                                                    }
                                                    EditingMode.HIDE -> viewModel.hideApp(app)
                                                    EditingMode.OPTIONS -> viewModel.showAppMenu(app)
                                                    EditingMode.NONE -> {
                                                        if (reorderState != null) viewModel.showAppMenu(app)
                                                        else handleAppLaunchRequest(app)
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                viewModel.showAppMenu(app)
                                            },
                                            onFocused = {
                                                isStartupParked = false
                                                when (app.packageName) {
                                                    "com.minidownloader.portal" -> viewModel.onFocusColorChanged(0xFFE91E63.toInt())
                                                    "com.aurastore.portal" -> viewModel.onFocusColorChanged(0xFF8B5CF6.toInt())
                                                    "com.miniyube.portal" -> viewModel.onFocusColorChanged(0xFFEF4444.toInt())
                                                    "com.crazygames.portal" -> viewModel.onFocusColorChanged(0xFF6366F1.toInt())
                                                    "com.poki.portal" -> viewModel.onFocusColorChanged(0xFF00D2FF.toInt())
                                                    "com.playhop.portal" -> viewModel.onFocusColorChanged(0xFF22C55E.toInt())
                                                    "com.easygames.portal" -> viewModel.onFocusColorChanged(0xFFEF4444.toInt())
                                                    else -> viewModel.onAppFocused(app)
                                                }
                                            }
                                        )
                                    }
                                }
                                if (rowApps.size < clampedAppsPerRow) {
                                    repeat(clampedAppsPerRow - rowApps.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }

                    if (gameApps.isNotEmpty()) {
                        item(key = "games_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 48.dp, end = 48.dp, top = 14.dp, bottom = 2.dp)
                            ) {
                                Icon(Icons.Default.SportsEsports, null, tint = TvAccentGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.title_games), color = TvTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                if (reorderState != null && reorderState?.items?.find { it.packageName == activeReorderPkg }?.isGame == true) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(stringResource(R.string.reorder_mode_hint), color = TvAccentGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        val isGamesFirstSection = isAppsFirstSection && nonGameApps.isEmpty()
                        itemsIndexed(
                            items = gameRows,
                            key = { rowIndex, _ -> "game_row_$rowIndex" }
                        ) { rowIndex, rowGames ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp),
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                rowGames.forEachIndexed { colIndex, app ->
                                    androidx.compose.runtime.key(app.packageName) {
                                        val isBeingReordered = app.packageName == activeReorderPkg
                                        val defaultFr = focusRequesters.getOrPut(app.packageName) { androidx.compose.ui.focus.FocusRequester() }
                                        val isFirstGameItem = isGamesFirstSection && rowIndex == 0 && colIndex == 0
                                        AppItemCard(
                                            app = app,
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(if (isFirstGameItem) Modifier.focusRequester(firstItemFocusRequester) else Modifier),
                                            isHighlighted = isBeingReordered,
                                            focusRequester = defaultFr,
                                            onRequestFocusUp = if (isGamesFirstSection && rowIndex == 0 && !isReorderingActive) {
                                                {
                                                    scrollHomeToBottomIdle()
                                                    try {
                                                        topBarFocusRequester.requestFocus()
                                                    } catch (_: Exception) {}
                                                }
                                            } else null,
                                            opacityPercent = glassOpacity,
                                            onClick = { 
                                                when (editingMode) {
                                                    EditingMode.MOVE -> viewModel.showAppMenu(app)
                                                    EditingMode.DELETE -> {
                                                        if (!app.packageName.endsWith(".portal")) {
                                                            viewModel.uninstallApp(context, app)
                                                        }
                                                    }
                                                    EditingMode.HIDE -> viewModel.hideApp(app)
                                                    EditingMode.OPTIONS -> viewModel.showAppMenu(app)
                                                    EditingMode.NONE -> {
                                                        if (reorderState != null) viewModel.showAppMenu(app)
                                                        else handleAppLaunchRequest(app)
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                viewModel.showAppMenu(app)
                                            },
                                            onFocused = {
                                                if (app.packageName == "com.minidownloader.portal") viewModel.onFocusColorChanged(0xFFE91E63.toInt())
                                                else if (app.packageName == "com.miniyube.portal") viewModel.onFocusColorChanged(0xFFEF4444.toInt())
                                                else if (app.packageName == "com.crazygames.portal") viewModel.onFocusColorChanged(0xFF6366F1.toInt())
                                                else if (app.packageName == "com.poki.portal") viewModel.onFocusColorChanged(0xFF00D2FF.toInt())
                                                else if (app.packageName == "com.playhop.portal") viewModel.onFocusColorChanged(0xFF22C55E.toInt())
                                                else if (app.packageName == "com.easygames.portal") viewModel.onFocusColorChanged(0xFFEF4444.toInt())
                                                else viewModel.onAppFocused(app)
                                            }
                                        )
                                    }
                                }
                                if (rowGames.size < clampedAppsPerRow) {
                                    repeat(clampedAppsPerRow - rowGames.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                }
            }
            }


            selectedAppForMenu?.let { app ->
                AppActionDialog(
                    app = app,
                    isReordering = reorderState != null,
                    onDismiss = { viewModel.dismissAppMenu() },
                    onOpen = {
                        viewModel.dismissAppMenu()
                        handleAppLaunchRequest(app)
                    },
                    onMove = { viewModel.startReorder(app) },
                    onConfirmMove = { viewModel.confirmReorder() },
                    onToggleGameCategory = { viewModel.toggleAppGameCategory(app) },
                    onToggleLock = {
                        viewModel.dismissAppMenu()
                        pendingLockedAppForToggle = app
                    },
                    onHide = { viewModel.hideApp(app) },
                    onDeleteBookmark = { viewModel.deleteWebBookmark(app) },
                    onInfo = { viewModel.openAppInfo(context, app) },
                    onUninstall = { viewModel.uninstallApp(context, app) }
                )
            }

            selectedChannelForMenu?.let { channel ->
                ChannelActionDialog(
                    channel = channel,
                    isReordering = channelReorderState != null,
                    onDismiss = { viewModel.dismissChannelMenu() },
                    onPlay = {
                        viewModel.dismissChannelMenu()
                        onOpenIptv(channel.id)
                    },
                    onMove = { viewModel.startChannelReorder(channel) },
                    onConfirmMove = { viewModel.confirmChannelReorder() },
                    onRemoveFavorite = { viewModel.toggleFavorite(channel) }
                )
            }

            pendingLockedAppForLaunch?.let { lockedApp ->
                AppLockPinDialog(
                    title = lockedApp.label,
                    subtitle = stringResource(R.string.pin_enter_to_open),
                    expectedPin = appLockPin,
                    onPinVerified = {
                        val target = lockedApp
                        pendingLockedAppForLaunch = null
                        executeAppOpen(target)
                    },
                    onDismiss = { pendingLockedAppForLaunch = null }
                )
            }

            pendingLockedAppForToggle?.let { targetApp ->
                AppLockPinDialog(
                    title = stringResource(if (targetApp.isLocked) R.string.action_unlock_app else R.string.action_lock_app),
                    subtitle = stringResource(R.string.pin_enter_to_confirm),
                    expectedPin = appLockPin,
                    onPinVerified = {
                        val appToToggle = targetApp
                        pendingLockedAppForToggle = null
                        viewModel.toggleAppLock(appToToggle)
                    },
                    onDismiss = { pendingLockedAppForToggle = null }
                )
            }

            if (showUniversalSearch) {
                UniversalSearchDialog(
                    apps = displayApps,
                    channels = (favoriteChannels + allChannels).distinctBy { it.id },
                    autoStartVoice = autoStartVoiceInSearch,
                    voiceTriggerTick = localVoiceTick,
                    onSelectApp = { app -> handleAppLaunchRequest(app) },
                    onSelectChannel = { ch -> onOpenIptv(ch.id) },
                    onOpenSettings = onOpenSettings,
                    onOpenIptv = { onOpenIptv(null) },
                    onDismiss = {
                        showUniversalSearch = false
                        autoStartVoiceInSearch = false
                    }
                )
            }

            if (showControlCenter) {
                QuickControlCenterDialog(
                    is24hFormat = isClock24h,
                    isDynamicBg = isDynamicBg,
                    onToggleClock24h = { viewModel.toggleClock24h() },
                    onToggleDynamicBg = { viewModel.toggleDynamicBg() },
                    onOpenAmbientMode = { showAmbientMode = true },
                    onOpenSearch = {
                        autoStartVoiceInSearch = true
                        localVoiceTick++
                        showUniversalSearch = true
                    },
                    onOpenSettings = onOpenSettings,
                    onDismiss = { showControlCenter = false }
                )
            }

            // Top Bar Overlay
            val isCurrentlyEditing = editingMode != EditingMode.NONE || reorderState != null || channelReorderState != null
            BackHandler(enabled = isCurrentlyEditing || !isStartupParked) {
                if (isCurrentlyEditing) {
                    if (reorderState != null) viewModel.confirmReorder()
                    if (channelReorderState != null) viewModel.confirmChannelReorder()
                    viewModel.setEditingMode(EditingMode.NONE)
                } else {
                    isStartupParked = true
                    viewModel.onNonAppFocused()
                    scrollHomeToBottomIdle()
                    try {
                        startupParkFocusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
            }
            TvTopBar(
                is24hFormat = isClock24h,
                showSeconds = isClockSecondsEnabled,
                clockSize = clockSize,
                dateFormatStyle = dateFormatStyle,
                greetingMode = greetingMode,
                customGreetingText = customGreetingText,
                onOpenSettings = onOpenSettings,
                onOpenIptv = { onOpenIptv(null) },
                onOpenYouTube = onOpenYouTube,
                onOpenSearch = {
                    autoStartVoiceInSearch = false
                    showUniversalSearch = true
                },
                onOpenControlCenter = { showControlCenter = true },
                isScrolled = isScrolled,
                isEditingMode = isCurrentlyEditing,
                onToggleEditMode = {
                    if (isCurrentlyEditing) {
                        if (reorderState != null) viewModel.confirmReorder()
                        if (channelReorderState != null) viewModel.confirmChannelReorder()
                        viewModel.setEditingMode(EditingMode.NONE)
                    } else {
                        viewModel.setEditingMode(EditingMode.MOVE)
                        focusFirstContentItem()
                    }
                },
                onNonAppFocused = {
                    if (!isReorderingActive) {
                        viewModel.onNonAppFocused()
                        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                            scrollHomeToBottomIdle()
                        }
                    }
                },
                onRequestFocusDown = {
                    focusFirstContentItem()
                },
                focusRequester = topBarFocusRequester,
                canFocusButtons = !isReorderingActive,
                glassOpacity = glassOpacity,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Bottom Floating Edit / Reorder Mode Banner
            AnimatedVisibility(
                visible = isCurrentlyEditing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0A0F1E).copy(alpha = 0.90f))
                        .border(1.5.dp, TvAccentGold.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenWith,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (reorderState != null || channelReorderState != null) {
                            stringResource(R.string.reorder_mode_hint)
                        } else {
                            stringResource(R.string.edit_mode_move_hint)
                        },
                        color = TvTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = showAmbientMode,
                enter = fadeIn(),
                exit = if (screensaverMode == "video_stream") androidx.compose.animation.ExitTransition.None else fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                AmbientScreensaverOverlay(
                    is24hFormat = isClock24h,
                    auroraColors = viewModel.defaultAuroraColors,
                    screensaverMode = screensaverMode,
                    screensaverVideoId = screensaverVideo,
                    wallpaperMode = if (screensaverWallpaper == "current") wallpaperMode else "custom",
                    customWallpaperUrl = if (screensaverWallpaper == "current") customWallpaperUrl else screensaverWallpaper,
                    onDismiss = {
                        lastInteractionTime = System.currentTimeMillis()
                        showAmbientMode = false
                    }
                )
            }
        }
    }
}

@Composable
private fun IsolatedHomeBackground(
    viewModel: HomeViewModel,
    isDynamicBg: Boolean,
    wallpaperMode: String,
    customWallpaperUrl: String
) {
    val focusedColor by viewModel.focusedColor.collectAsState()
    val auroraColors by viewModel.auroraColors.collectAsState()

    DynamicBackground(
        targetColorInt = focusedColor,
        auroraColors = auroraColors,
        isEnabled = isDynamicBg,
        wallpaperMode = wallpaperMode,
        customWallpaperUrl = customWallpaperUrl
    ) {}
}
