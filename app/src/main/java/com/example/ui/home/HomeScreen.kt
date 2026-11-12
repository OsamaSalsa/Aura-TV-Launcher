package com.example.ui.home

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.R
import com.example.data.model.AppItem
import com.example.data.model.IptvChannel
import com.example.ui.common.DynamicBackground
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvActionButton
import com.example.ui.common.TvTopBar
import com.example.ui.common.GlassSheenOverlay
import com.example.ui.common.drawableToBitmapSafe
import com.example.ui.common.safeBlur
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

    LaunchedEffect(voiceSearchTriggerTick) {
        if (voiceSearchTriggerTick > 0) {
            autoStartVoiceInSearch = true
            localVoiceTick++
            showUniversalSearch = true
        }
    }

    fun executeAppOpen(app: AppItem) {
        when (app.packageName) {
            "com.minidownloader.portal" -> onOpenMiniDownloader()
            "com.aurastore.portal" -> onOpenAuraStore()
            "com.miniyube.portal" -> onOpenYouTube()
            "com.crazygames.portal" -> onOpenCrazyGames()
            "com.poki.portal" -> onOpenPoki()
            "com.playhop.portal" -> onOpenPlayhop()
            "com.easygames.portal" -> onOpenEasyGames()
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

    var showStoragePermissionPrompt by rememberSaveable {
        mutableStateOf(!FilePermissionHelper.hasStoragePermission(context))
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

    if (showLauncherPrompt && !viewModel.isDefaultLauncher(context)) {
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
                        scaleUpRatio = 1.05f,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_later),
                                color = if (isFocused) TvAccentGold else TvTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

    if (!showLauncherPrompt && showStoragePermissionPrompt && !FilePermissionHelper.hasStoragePermission(context)) {
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
                        scaleUpRatio = 1.05f,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.action_later),
                                color = if (isFocused) TvAccentGold else TvTextSecondary,
                                fontSize = 14.sp
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
    
    val focusRequesters = remember { mutableMapOf<String, androidx.compose.ui.focus.FocusRequester>() }
    val channelFocusRequesters = remember { mutableMapOf<Long, androidx.compose.ui.focus.FocusRequester>() }
    val channelsRowState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(activeReorderPkg, activeReorderChannelId, displayApps, displayChannels) {
        activeReorderPkg?.let { pkg ->
            try {
                focusRequesters[pkg]?.requestFocus()
            } catch (_: Exception) {}
        }
        activeReorderChannelId?.let { id ->
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

    val clampedAppsPerRow = appsPerRow.coerceIn(4, 7)
    val nonGameApps = remember(displayApps) { displayApps.filter { !it.isGame } }
    val gameApps = remember(displayApps) { displayApps.filter { it.isGame } }
    val nonGameRows = remember(nonGameApps, clampedAppsPerRow) { nonGameApps.chunked(clampedAppsPerRow) }
    val gameRows = remember(gameApps, clampedAppsPerRow) { gameApps.chunked(clampedAppsPerRow) }

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
                    if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                        if (native.keyCode == android.view.KeyEvent.KEYCODE_MENU || native.keyCode == android.view.KeyEvent.KEYCODE_SETTINGS) {
                            onOpenSettings()
                            true
                        } else if (native.keyCode == android.view.KeyEvent.KEYCODE_SEARCH) {
                            showUniversalSearch = true
                            true
                        } else false
                    } else false
                }
        ) {
            val totalWidth = maxWidth
            val horizontalPadding = 48.dp * 2
            val itemSpacing = 18.dp
            val cardHeight = (totalWidth - horizontalPadding - (itemSpacing * (appsPerRow - 1))) / appsPerRow * 0.5625f
            
            val firstItemFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            val topBarFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            val coroutineScope = rememberCoroutineScope()
            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            val isScrolled by remember { androidx.compose.runtime.derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 } }

            fun focusFirstContentItem() {
                coroutineScope.launch {
                    try {
                        if (listState.firstVisibleItemIndex > 0) {
                            listState.scrollToItem(0)
                            kotlinx.coroutines.delay(60)
                        }
                        if (favoriteChannels.isNotEmpty()) {
                            channelsRowState.scrollToItem(0)
                            kotlinx.coroutines.delay(40)
                        }
                        try {
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
                    } catch (_: Exception) {
                        try {
                            focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
                        } catch (_: Exception) {}
                    }
                }
            }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(150)
                try {
                    topBarFocusRequester.requestFocus()
                } catch (_: Exception) {}
            }

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = appsTopOffset.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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
                                LazyRow(
                                    state = channelsRowState,
                                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    itemsIndexed(displayChannels, key = { _, channel -> channel.id }) { index, channel ->
                                        val isBeingReordered = channel.id == activeReorderChannelId
                                        val defaultFr = channelFocusRequesters.getOrPut(channel.id) { androidx.compose.ui.focus.FocusRequester() }
                                        val fr = if (index == 0 && activeReorderChannelId == null) firstItemFocusRequester else defaultFr
                                        
                                        FavoriteChannelCard(
                                            channel = channel,
                                            cardSize = cardHeight.coerceAtLeast(100.dp),
                                            opacityPercent = glassOpacity,
                                            isHighlighted = isBeingReordered,
                                            focusRequester = fr,
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
                                            onFocused = { viewModel.onNonAppFocused() },
                                            onMoveLeft = { if (isRtl) viewModel.moveChannelRight() else viewModel.moveChannelLeft() },
                                            onMoveRight = { if (isRtl) viewModel.moveChannelLeft() else viewModel.moveChannelRight() },
                                            onRequestFocusUp = {
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(0)
                                                }
                                                try {
                                                    topBarFocusRequester.requestFocus()
                                                } catch (_: Exception) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (nonGameApps.isNotEmpty()) {
                        item(key = "apps_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 48.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Apps, null, tint = TvAccentGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.title_apps), color = TvTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                if (reorderState != null && reorderState?.items?.find { it.packageName == activeReorderPkg }?.isGame == false) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(stringResource(R.string.reorder_mode_hint), color = TvAccentGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        itemsIndexed(
                            items = nonGameRows,
                            key = { rowIndex, rowApps -> "app_row_${rowIndex}_${rowApps.firstOrNull()?.packageName.orEmpty()}" }
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
                                        val fr = if (favoriteChannels.isEmpty() && rowIndex == 0 && colIndex == 0) firstItemFocusRequester else defaultFr
                                        AppItemCard(
                                            app = app,
                                            modifier = Modifier.weight(1f),
                                            isHighlighted = isBeingReordered,
                                            focusRequester = fr,
                                            onRequestFocusUp = if (favoriteChannels.isEmpty() && rowIndex == 0) {
                                                {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(0)
                                                    }
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

                        itemsIndexed(
                            items = gameRows,
                            key = { rowIndex, rowGames -> "game_row_${rowIndex}_${rowGames.firstOrNull()?.packageName.orEmpty()}" }
                        ) { _, rowGames ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp),
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                rowGames.forEach { app ->
                                    androidx.compose.runtime.key(app.packageName) {
                                        val isBeingReordered = app.packageName == activeReorderPkg
                                        val defaultFr = focusRequesters.getOrPut(app.packageName) { androidx.compose.ui.focus.FocusRequester() }
                                        val fr = if (favoriteChannels.isEmpty() && nonGameApps.isEmpty() && app == gameApps.firstOrNull()) firstItemFocusRequester else defaultFr
                                        AppItemCard(
                                            app = app,
                                            modifier = Modifier.weight(1f),
                                            isHighlighted = isBeingReordered,
                                            focusRequester = fr,
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


            selectedAppForMenu?.let { app ->
                AppActionDialog(
                    app = app,
                    isReordering = reorderState != null,
                    onDismiss = { viewModel.dismissAppMenu() },
                    onMove = { viewModel.startReorder(app) },
                    onConfirmMove = { viewModel.confirmReorder() },
                    onToggleGameCategory = { viewModel.toggleAppGameCategory(app) },
                    onToggleLock = {
                        viewModel.dismissAppMenu()
                        pendingLockedAppForToggle = app
                    },
                    onHide = { viewModel.hideApp(app) },
                    onInfo = { viewModel.openAppInfo(context, app) },
                    onUninstall = { viewModel.uninstallApp(context, app) }
                )
            }

            selectedChannelForMenu?.let { channel ->
                ChannelActionDialog(
                    channel = channel,
                    isReordering = channelReorderState != null,
                    onDismiss = { viewModel.dismissChannelMenu() },
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
                onNonAppFocused = { viewModel.onNonAppFocused() },
                onRequestFocusDown = {
                    focusFirstContentItem()
                },
                focusRequester = topBarFocusRequester,
                glassOpacity = glassOpacity,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            AnimatedVisibility(
                visible = showAmbientMode,
                enter = fadeIn(),
                exit = fadeOut(),
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

private val AppCardShape = RoundedCornerShape(16.dp)

@Composable
fun AppItemCard(
    app: AppItem,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() },
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onFocused: () -> Unit,
    onRequestFocusUp: (() -> Unit)? = null,
    opacityPercent: Int = 75
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onLongClick,
        onFocused = onFocused,
        focusRequester = focusRequester,
        scaleUpRatio = 1.07f,
        elevation = 8.dp,
        useDefaultBorder = false,
        modifier = modifier
            .aspectRatio(1.77f)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                    val isMenuKey = native.keyCode == android.view.KeyEvent.KEYCODE_MENU ||
                            native.keyCode == android.view.KeyEvent.KEYCODE_SETTINGS ||
                            native.keyCode == 82 ||
                            native.keyCode == 176
                    if (isMenuKey) {
                        onLongClick?.invoke()
                        true
                    } else if (keyEvent.type == androidx.compose.ui.input.key.KeyEventType.KeyDown && 
                        keyEvent.key == androidx.compose.ui.input.key.Key.DirectionUp
                    ) {
                        if (onRequestFocusUp != null) {
                            onRequestFocusUp()
                            true
                        } else false
                    } else false
                } else false
            }
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(AppCardShape)
        ) {
            if (app.packageName == "com.minidownloader.portal") {
                Image(painter = painterResource(R.drawable.ic_minidownloader_banner), "Mini Downloader", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.aurastore.portal") {
                Image(painter = painterResource(R.drawable.ic_aurastore_banner), "Aura Store", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.miniyube.portal") {
                Image(painter = painterResource(R.drawable.ic_minitube_banner), "MiniTube", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.crazygames.portal") {
                Image(painter = painterResource(R.drawable.ic_crazygames_banner), "CrazyGames", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.poki.portal") {
                Image(painter = painterResource(R.drawable.ic_poki_banner), "Poki", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.playhop.portal") {
                Image(painter = painterResource(R.drawable.ic_playhop_banner), "Playhop", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.easygames.portal") {
                Image(painter = painterResource(R.drawable.ic_easygames_banner), "EasyGames", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                val bannerImageBitmap = app.bannerBitmap ?: remember(app.banner) {
                    app.banner?.let { drawableToBitmapSafe(it).asImageBitmap() }
                }
                val iconImageBitmap = app.iconBitmap ?: remember(app.icon) {
                    app.icon?.let { drawableToBitmapSafe(it).asImageBitmap() }
                }

                if (bannerImageBitmap != null) {
                    Image(bitmap = bannerImageBitmap, app.label, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    val domColor = app.dominantColor?.let { Color(it) } ?: TvAccentBlue
                    val glowColor = TvAccentBlue
                    val deepColor = TvDeepBlue
                    val glassBg = Color(0x331E293B)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(glassBg),
                        contentAlignment = Alignment.Center
                    ) {
                        // Fast radial gradient bloom matching the icon's palette (no expensive per-card blur)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            drawRect(
                                brush = Brush.radialGradient(
                                    0.0f to domColor.copy(alpha = 0.50f),
                                    0.55f to deepColor.copy(alpha = 0.25f),
                                    1.0f to Color.Transparent,
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    radius = w * 0.85f
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0.0f to glowColor.copy(alpha = 0.35f),
                                    0.55f to glowColor.copy(alpha = 0.12f),
                                    1.0f to Color.Transparent,
                                    center = Offset(w * 0.50f, h * 0.35f),
                                    radius = w * 0.60f
                                )
                            )
                        }

                        // Dark vignette overlay to ensure text and icon stand out with high contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.12f),
                                            Color.Black.copy(alpha = 0.42f)
                                        )
                                    )
                                )
                        )

                        // Centered sharp icon and application label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (iconImageBitmap != null) {
                                Image(
                                    bitmap = iconImageBitmap,
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .padding(bottom = 4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                text = app.label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (app.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(1.dp, TvAccentGold.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Glass Sheen & Prismatic Bevel Overlay
            GlassSheenOverlay(isFocused = isFocused || isHighlighted, shape = AppCardShape)
        }
    }
}

@Composable
fun FavoriteChannelCard(
    channel: IptvChannel,
    cardSize: androidx.compose.ui.unit.Dp = 110.dp,
    opacityPercent: Int = 75,
    isHighlighted: Boolean = false,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() },
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFocused: () -> Unit,
    onMoveLeft: (() -> Unit)? = null,
    onMoveRight: (() -> Unit)? = null,
    onRequestFocusUp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onLongClick,
        onFocused = onFocused,
        focusRequester = focusRequester,
        scaleUpRatio = if (isHighlighted) 1.10f else 1.08f,
        elevation = 8.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = modifier
            .size(cardSize)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                    val isMenuKey = native.keyCode == android.view.KeyEvent.KEYCODE_MENU ||
                            native.keyCode == android.view.KeyEvent.KEYCODE_SETTINGS ||
                            native.keyCode == 82 ||
                            native.keyCode == 176
                    if (isMenuKey) {
                        onLongClick()
                        true
                    } else if (keyEvent.type == androidx.compose.ui.input.key.KeyEventType.KeyDown) {
                        if (keyEvent.key == androidx.compose.ui.input.key.Key.DirectionUp && !isHighlighted) {
                            if (onRequestFocusUp != null) {
                                onRequestFocusUp()
                                true
                            } else false
                        } else if (isHighlighted) {
                            if (keyEvent.key == androidx.compose.ui.input.key.Key.DirectionLeft) {
                                onMoveLeft?.invoke()
                                true
                            } else if (keyEvent.key == androidx.compose.ui.input.key.Key.DirectionRight) {
                                onMoveRight?.invoke()
                                true
                            } else false
                        } else false
                    } else false
                } else false
            }
    ) { isFocused ->
        val glassAlpha = (opacityPercent.coerceIn(10, 100) / 100f)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Translucent Glass Tile - Original Style with variable white transparency
            val channelShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(channelShape)
                    .background(Color.White.copy(alpha = glassAlpha))
                    .border(
                        width = if (isHighlighted || isFocused) 2.5.dp else 0.dp,
                        color = if (isHighlighted || isFocused) TvAccentGold else Color.Transparent,
                        shape = channelShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        channel.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Glass Sheen & Beveled Refractive Rim Overlay
                GlassSheenOverlay(
                    isFocused = isFocused || isHighlighted,
                    shape = channelShape
                )
            }

            // 3. Move Indicator (◀ نقل ▶) on the back frame when moving
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, TvAccentGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Move Left",
                            tint = TvAccentGold,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_move),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Move Right",
                            tint = TvAccentGold,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaHubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    badge: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    TvFocusableCard(onClick = onClick, onFocused = onFocused, scaleUpRatio = 1.06f, modifier = modifier.height(130.dp)) { isFocused ->
        val cardShape = RoundedCornerShape(16.dp)
        Box(modifier = Modifier.fillMaxSize().clip(cardShape).background(gradient)) {
            Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                        }
                    }
                    Column {
                        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            GlassSheenOverlay(isFocused = isFocused, shape = cardShape)
        }
    }
}
