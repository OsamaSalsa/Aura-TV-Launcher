package com.example.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppItem
import com.example.data.model.IptvChannel
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvTextField
import com.example.ui.common.drawableToBitmapSafe
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

private enum class SearchFilter {
    ALL, APPS, GAMES, CHANNELS
}

private enum class VoiceSystemAction {
    OPEN_SETTINGS, OPEN_IPTV
}

private data class ParsedVoiceQuery(
    val effectiveQuery: String,
    val isActionCommand: Boolean,
    val systemAction: VoiceSystemAction? = null
)

private fun parseSmartVoiceQuery(raw: String): ParsedVoiceQuery {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ParsedVoiceQuery("", false, null)

    val normalized = trimmed.lowercase(Locale.getDefault())
    val prefixes = listOf(
        "افتح لي ", "شغل لي ", "شغلي ", "افتح ", "شغّل ", "شغل ", "ابغى ", "أبغى ", "قناة ", "قناه ",
        "open ", "launch ", "start ", "play ", "watch ", "go to ", "channel ",
        "öffne ", "starte ", "spiele ", "zeige ", "sender ",
        "abrir ", "abre ", "iniciar ", "reproduzir ", "canal ",
        "aç ", "başlat ", "oynat ", "kanal ",
        "buka ", "putar ", "jalankan ", "saluran ",
        "کھولیں ", "کھولو ", "چلائیں ", "چلاؤ ", "چینل ",
        "باز کن ", "پخش کن ", "اجرا کن ", "کانال ", "شبکه ",
        "otwórz ", "uruchom ", "włącz ", "kanał ",
        "openen ", "speel ", "bekijk ", "kanaal ",
        "mở ", "phát ", "chạy ", "kênh ",
        "เปิด", "เล่น", "ช่อง",
        "άνοιξε ", "παίξε ", "κανάλι ",
        "打开", "播放", "启动", "观看", "频道",
        "開いて", "再生", "起動", "チャンネル",
        "열어 ", "실행 ", "재생 ", "틀어 ", "채널 ",
        "खोलो ", "चलाओ ", "देखो ", "चैनल "
    )

    var cleaned = trimmed
    var isAction = false
    for (prefix in prefixes) {
        if (normalized.startsWith(prefix)) {
            cleaned = trimmed.substring(prefix.length).trim()
            isAction = true
            break
        }
    }

    val targetLower = cleaned.lowercase(Locale.getDefault())
    val sysAction = when {
        targetLower in setOf(
            "الاعدادات", "الإعدادات", "اعدادات", "إعدادات",
            "settings", "paramètres", "impostazioni", "ajustes", "настройки",
            "einstellungen", "设置", "設定", "설정", "सेटिंग्स",
            "configurações", "definições", "ayarlar", "pengaturan", "setelan",
            "ترتیبات", "تنظیمات", "ustawienia", "instellingen", "cài đặt", "การตั้งค่า", "ρυθμίσεις"
        ) ->
            VoiceSystemAction.OPEN_SETTINGS
        targetLower in setOf(
            "تلفزيون", "التلفزيون", "بث مباشر", "قنوات", "القنوات",
            "iptv", "tv", "live tv", "channels",
            "fernsehen", "sender", "电视", "直播", "テレビ", "ライブ", "티비", "방송", "टीवी", "चैनल",
            "canais", "televisão", "canlı tv", "kanallar", "saluran tv",
            "ٹی وی", "چینلز", "تلویزیون", "کانال‌ها", "telewizja", "kanały", "zenders", "truyền hình", "ทีวี", "τηλεόραση", "κανάλια"
        ) ->
            VoiceSystemAction.OPEN_IPTV
        else -> null
    }

    return ParsedVoiceQuery(
        effectiveQuery = cleaned.ifBlank { trimmed },
        isActionCommand = isAction || sysAction != null,
        systemAction = sysAction
    )
}

@Composable
fun UniversalSearchDialog(
    apps: List<AppItem>,
    channels: List<IptvChannel>,
    autoStartVoice: Boolean = false,
    voiceTriggerTick: Int = 0,
    onSelectApp: (AppItem) -> Unit,
    onSelectChannel: (IptvChannel) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenIptv: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var partialVoiceText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SearchFilter.ALL) }
    var isListening by remember { mutableStateOf(false) }
    var voiceStatusMessage by remember { mutableStateOf<String?>(null) }
    var voiceRmsLevel by remember { mutableFloatStateOf(0f) }

    val micButtonFocusRequester = remember { FocusRequester() }
    val speechRecognizerRef = remember { mutableStateOf<SpeechRecognizer?>(null) }

    val listeningPromptText = stringResource(R.string.voice_search_listening)
    val speakNowHintText = stringResource(R.string.voice_search_speak_now)
    val noSpeechErrorText = stringResource(R.string.voice_search_try_again)
    val micPermissionDeniedText = stringResource(R.string.voice_search_permission_needed)

    var wasVoiceResult by remember { mutableStateOf(false) }
    var autoLaunchCancelled by remember { mutableStateOf(false) }

    // System RecognizerIntent activity fallback for Android TV boxes without direct service binding
    val fallbackVoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        voiceRmsLevel = 0f
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val best = matches?.firstOrNull()
            if (!best.isNullOrBlank()) {
                autoLaunchCancelled = false
                wasVoiceResult = true
                query = best
                partialVoiceText = ""
                voiceStatusMessage = null
            }
        }
    }

    fun stopVoiceRecognition() {
        isListening = false
        voiceRmsLevel = 0f
        try {
            speechRecognizerRef.value?.stopListening()
            speechRecognizerRef.value?.cancel()
        } catch (_: Exception) {}
    }

    val currentConfig = LocalConfiguration.current
    var hasTriedFallbackActivity by remember { mutableStateOf(false) }

    fun launchSystemVoiceActivity(localeTag: String) {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PROMPT, speakNowHintText)
            }
            fallbackVoiceLauncher.launch(intent)
        } catch (_: Exception) {
            isListening = false
            voiceRmsLevel = 0f
            voiceStatusMessage = noSpeechErrorText
        }
    }

    fun startVoiceRecognitionInternal() {
        partialVoiceText = ""
        voiceStatusMessage = listeningPromptText
        isListening = true
        voiceRmsLevel = 0.25f
        hasTriedFallbackActivity = false

        val configLocale = if (!currentConfig.locales.isEmpty) currentConfig.locales[0] else Locale.getDefault()
        val localeTag = configLocale.toLanguageTag().ifBlank { "ar-SA" }

        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                val recognizer = speechRecognizerRef.value ?: SpeechRecognizer.createSpeechRecognizer(context).also {
                    speechRecognizerRef.value = it
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        voiceStatusMessage = speakNowHintText
                    }

                    override fun onBeginningOfSpeech() {
                        isListening = true
                        voiceStatusMessage = listeningPromptText
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB (-2..12) to 0.08..1.0 for smooth wave animation
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1f)
                        voiceRmsLevel = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        voiceRmsLevel = 0f
                    }

                    override fun onError(error: Int) {
                        val isServiceBindingFailure = error == SpeechRecognizer.ERROR_CLIENT ||
                            error == SpeechRecognizer.ERROR_AUDIO ||
                            error == SpeechRecognizer.ERROR_SERVER ||
                            error == 11 // ERROR_SERVER_DISCONNECTED
                        if (isServiceBindingFailure && partialVoiceText.isBlank() && !hasTriedFallbackActivity) {
                            hasTriedFallbackActivity = true
                            launchSystemVoiceActivity(localeTag)
                            return
                        }
                        isListening = false
                        voiceRmsLevel = 0f
                        if (partialVoiceText.isNotBlank()) {
                            query = partialVoiceText
                            partialVoiceText = ""
                            voiceStatusMessage = null
                        } else {
                            voiceStatusMessage = noSpeechErrorText
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        voiceRmsLevel = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull()
                        if (!spoken.isNullOrBlank()) {
                            autoLaunchCancelled = false
                            wasVoiceResult = true
                            query = spoken
                            partialVoiceText = ""
                            voiceStatusMessage = null
                        } else if (partialVoiceText.isNotBlank()) {
                            autoLaunchCancelled = false
                            wasVoiceResult = true
                            query = partialVoiceText
                            partialVoiceText = ""
                            voiceStatusMessage = null
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            partialVoiceText = partial
                            query = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                recognizer.cancel()
                recognizer.startListening(recognizerIntent)
            } else {
                // Fallback to system speech recognition activity
                hasTriedFallbackActivity = true
                launchSystemVoiceActivity(localeTag)
            }
        } catch (_: Exception) {
            hasTriedFallbackActivity = true
            launchSystemVoiceActivity(localeTag)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceRecognitionInternal()
        } else {
            isListening = false
            voiceStatusMessage = micPermissionDeniedText
        }
    }

    fun triggerVoiceSearch() {
        if (isListening) {
            stopVoiceRecognition()
            voiceStatusMessage = null
        } else {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                startVoiceRecognitionInternal()
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizerRef.value?.destroy()
                speechRecognizerRef.value = null
            } catch (_: Exception) {}
        }
    }

    // Automatically start listening if opened via Voice Search remote button or voiceTriggerTick increments
    LaunchedEffect(autoStartVoice, voiceTriggerTick) {
        if (autoStartVoice || voiceTriggerTick > 0) {
            delay(120)
            try {
                micButtonFocusRequester.requestFocus()
            } catch (_: Exception) {}
            if (!isListening) {
                triggerVoiceSearch()
            }
        }
    }

    val parsedVoice = remember(query) { parseSmartVoiceQuery(query) }
    val effectiveQuery = parsedVoice.effectiveQuery

    val filteredApps = remember(apps, effectiveQuery, filter) {
        if (filter == SearchFilter.CHANNELS) emptyList()
        else apps.filter { app ->
            val matchesCategory = when (filter) {
                SearchFilter.APPS -> !app.isGame
                SearchFilter.GAMES -> app.isGame
                else -> true
            }
            val matchesQuery = effectiveQuery.isBlank() ||
                    app.label.contains(effectiveQuery, ignoreCase = true) ||
                    app.packageName.contains(effectiveQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val filteredChannels = remember(channels, effectiveQuery, filter) {
        if (filter == SearchFilter.APPS || filter == SearchFilter.GAMES) emptyList()
        else channels.filter { ch ->
            effectiveQuery.isBlank() ||
                    ch.name.contains(effectiveQuery, ignoreCase = true) ||
                    ch.groupTitle.contains(effectiveQuery, ignoreCase = true)
        }.take(40)
    }

    val totalMatches = filteredApps.size + filteredChannels.size
    val pendingVoiceTargetLabel: String? = remember(
        wasVoiceResult,
        autoLaunchCancelled,
        isListening,
        parsedVoice,
        filteredApps,
        filteredChannels
    ) {
        if (!wasVoiceResult || autoLaunchCancelled || isListening) return@remember null
        when {
            parsedVoice.systemAction == VoiceSystemAction.OPEN_SETTINGS -> "Settings"
            parsedVoice.systemAction == VoiceSystemAction.OPEN_IPTV -> "IPTV"
            parsedVoice.isActionCommand && filteredApps.size == 1 && filteredChannels.isEmpty() -> filteredApps[0].label
            parsedVoice.isActionCommand && filteredChannels.size == 1 && filteredApps.isEmpty() -> filteredChannels[0].name
            totalMatches == 1 && filteredApps.size == 1 -> filteredApps[0].label
            totalMatches == 1 && filteredChannels.size == 1 -> filteredChannels[0].name
            else -> null
        }
    }

    LaunchedEffect(pendingVoiceTargetLabel, wasVoiceResult, autoLaunchCancelled, isListening) {
        if (pendingVoiceTargetLabel != null) {
            delay(950L)
            if (wasVoiceResult && !autoLaunchCancelled && !isListening) {
                when {
                    parsedVoice.systemAction == VoiceSystemAction.OPEN_SETTINGS -> {
                        onDismiss()
                        onOpenSettings()
                    }
                    parsedVoice.systemAction == VoiceSystemAction.OPEN_IPTV -> {
                        onDismiss()
                        onOpenIptv()
                    }
                    filteredApps.size == 1 && filteredChannels.isEmpty() -> {
                        val target = filteredApps[0]
                        onDismiss()
                        onSelectApp(target)
                    }
                    filteredChannels.size == 1 && filteredApps.isEmpty() -> {
                        val target = filteredChannels[0]
                        onDismiss()
                        onSelectChannel(target)
                    }
                }
            }
        }
    }

    val quickKeys = remember {
        listOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "ا", "ب", "ت", "ج", "ح", "د", "ر", "س", "ش", "ص", "ع", "ف", "ق", "ك", "ل", "م", "ن", "ه", "و", "ي",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        )
    }

    val dialogContext = LocalContext.current
    val currentLayoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val currentConfiguration = androidx.compose.ui.platform.LocalConfiguration.current

    Dialog(
        onDismissRequest = {
            stopVoiceRecognition()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalContext provides dialogContext,
            androidx.compose.ui.platform.LocalConfiguration provides currentConfiguration,
            androidx.compose.ui.platform.LocalLayoutDirection provides currentLayoutDirection
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
            color = TvMidnightBackground.copy(alpha = 0.98f),
            shadowElevation = 28.dp,
            modifier = Modifier
                .width(880.dp)
                .height(575.dp)
                .padding(16.dp)
                .border(
                    width = if (isListening) 2.dp else 1.5.dp,
                    color = if (isListening) TvAccentGold else Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(28.dp)
                )
                .onKeyEvent { keyEvent ->
                    val native = keyEvent.nativeKeyEvent
                    val isVoiceKey = native.keyCode == android.view.KeyEvent.KEYCODE_VOICE_ASSIST ||
                            native.keyCode == android.view.KeyEvent.KEYCODE_SEARCH ||
                            native.keyCode == android.view.KeyEvent.KEYCODE_ASSIST
                    if (isVoiceKey) {
                        if (keyEvent.type == KeyEventType.KeyDown && native.repeatCount == 0) {
                            triggerVoiceSearch()
                        }
                        true
                    } else false
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header & Search Bar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Voice Search Microphone Button with Pulsing Ring
                    VoiceMicTriggerButton(
                        isListening = isListening,
                        focusRequester = micButtonFocusRequester,
                        onClick = { triggerVoiceSearch() }
                    )

                    TvTextField(
                        value = query,
                        onValueChange = {
                            wasVoiceResult = false
                            autoLaunchCancelled = true
                            query = it
                            if (isListening) stopVoiceRecognition()
                        },
                        placeholder = if (isListening) {
                            stringResource(R.string.voice_search_speak_now)
                        } else {
                            stringResource(R.string.universal_search_hint)
                        },
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    )

                    // Backspace button
                    TvFocusableCard(
                        onClick = {
                            wasVoiceResult = false
                            if (query.isNotEmpty()) query = query.dropLast(1)
                        },
                        modifier = Modifier.size(48.dp)
                    ) { isFocused ->
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = if (isFocused) TvAccentGold else TvTextPrimary,
                            modifier = Modifier.align(Alignment.Center).size(20.dp)
                        )
                    }

                    // Close button
                    TvFocusableCard(
                        onClick = {
                            stopVoiceRecognition()
                            onDismiss()
                        },
                        modifier = Modifier.size(48.dp)
                    ) { isFocused ->
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = if (isFocused) TvAccentGold else TvTextPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Animated Voice Listening Visualizer Stage
                AnimatedVisibility(
                    visible = isListening || voiceStatusMessage != null,
                    enter = fadeIn(tween(220)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(180)) + shrinkVertically(tween(220))
                ) {
                    VoiceListeningStageBanner(
                        isListening = isListening,
                        rmsLevel = voiceRmsLevel,
                        statusText = voiceStatusMessage ?: stringResource(R.string.voice_search_listening),
                        liveTranscript = partialVoiceText.ifBlank { query },
                        onStopClick = {
                            stopVoiceRecognition()
                            voiceStatusMessage = null
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                // Smart Voice Command Auto-Launch Countdown Banner
                AnimatedVisibility(
                    visible = pendingVoiceTargetLabel != null,
                    enter = fadeIn(tween(180)) + expandVertically(tween(200)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(160))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TvAccentGold.copy(alpha = 0.16f))
                            .border(1.dp, TvAccentGold.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(R.string.voice_command_opening)}: ${pendingVoiceTargetLabel.orEmpty()}",
                            color = TvAccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TvFocusableCard(
                            onClick = { autoLaunchCancelled = true },
                            shape = RoundedCornerShape(10.dp),
                            cardBackgroundColor = Color.White.copy(alpha = 0.12f)
                        ) { isFocused ->
                            Text(
                                text = stringResource(R.string.action_cancel),
                                color = if (isFocused) TvAccentGold else TvTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // D-Pad Quick Character Bar
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        TvFocusableCard(
                            onClick = {
                                query = ""
                                partialVoiceText = ""
                            },
                            scaleUpRatio = 1.06f,
                            modifier = Modifier.height(34.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isFocused) TvAccentGold else Color(0xFF1E293B),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.action_reset),
                                    color = if (isFocused) Color.Black else TvAccentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    item {
                        TvFocusableCard(
                            onClick = { query += " " },
                            scaleUpRatio = 1.06f,
                            modifier = Modifier.height(34.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isFocused) TvAccentGold else Color(0xFF1E293B),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "␣",
                                    color = if (isFocused) Color.Black else TvTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    items(quickKeys) { ch ->
                        TvFocusableCard(
                            onClick = { query += ch },
                            scaleUpRatio = 1.08f,
                            modifier = Modifier.size(34.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) TvAccentGold else Color(0xFF1E293B),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ch,
                                    color = if (isFocused) Color.Black else TvTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf(
                        SearchFilter.ALL to stringResource(R.string.universal_search_all),
                        SearchFilter.APPS to stringResource(R.string.title_apps),
                        SearchFilter.GAMES to stringResource(R.string.title_games),
                        SearchFilter.CHANNELS to stringResource(R.string.iptv_title)
                    )
                    filters.forEach { (f, title) ->
                        val isSelected = (filter == f)
                        TvFocusableCard(
                            onClick = { filter = f },
                            scaleUpRatio = 1.04f,
                            modifier = Modifier.height(36.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        when {
                                            isFocused -> TvAccentGold
                                            isSelected -> TvAccentGold.copy(alpha = 0.25f)
                                            else -> Color(0xFF1E293B).copy(alpha = 0.7f)
                                        },
                                        RoundedCornerShape(18.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected || isFocused) TvAccentGold else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isFocused) Color.Black else if (isSelected) TvAccentGold else TvTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results Grid
                if (filteredApps.isEmpty() && filteredChannels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.iptv_empty),
                            color = TvTextSecondary,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 190.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredApps, key = { "app_${it.packageName}" }) { app ->
                            SearchResultAppTile(
                                app = app,
                                onClick = {
                                    stopVoiceRecognition()
                                    onDismiss()
                                    onSelectApp(app)
                                }
                            )
                        }
                        items(filteredChannels, key = { "ch_${it.id}" }) { ch ->
                            SearchResultChannelTile(
                                channel = ch,
                                onClick = {
                                    stopVoiceRecognition()
                                    onDismiss()
                                    onSelectChannel(ch)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun VoiceMicTriggerButton(
    isListening: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )

    TvFocusableCard(
        onClick = onClick,
        focusRequester = focusRequester,
        scaleUpRatio = 1.08f,
        useDefaultBorder = false,
        modifier = Modifier.size(54.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isListening) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = (size.minDimension / 2f) * pulseRadius
                    drawCircle(
                        color = TvAccentRed.copy(alpha = 0.28f),
                        radius = r
                    )
                    drawCircle(
                        color = TvAccentGold.copy(alpha = 0.45f),
                        radius = r * 0.85f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isListening -> Brush.linearGradient(listOf(TvAccentRed, Color(0xFFF59E0B)))
                            isFocused -> Brush.linearGradient(listOf(TvAccentGold, Color(0xFFF59E0B)))
                            else -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                        }
                    )
                    .border(
                        width = if (isFocused || isListening) 2.dp else 1.dp,
                        color = if (isListening) Color.White else if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.22f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.voice_search_button),
                    tint = if (isListening) Color.White else if (isFocused) Color.Black else TvAccentGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceListeningStageBanner(
    isListening: Boolean,
    rmsLevel: Float,
    statusText: String,
    liveTranscript: String,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_stage_waves")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    val animatedRms by animateFloatAsState(
        targetValue = if (isListening) rmsLevel.coerceAtLeast(0.22f) else 0.05f,
        animationSpec = tween(120),
        label = "animated_rms"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1E1B4B).copy(alpha = 0.92f),
                        Color(0xFF0F172A).copy(alpha = 0.95f),
                        Color(0xFF1E1B4B).copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        TvAccentBlue.copy(alpha = 0.7f),
                        TvAccentGold,
                        TvAccentRed.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Pulsing Microphone Orb + Concentric Ripples
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isListening) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxR = size.minDimension / 2f
                        drawCircle(
                            color = TvAccentGold.copy(alpha = (1.45f - ringScale).coerceIn(0f, 0.45f)),
                            radius = maxR * ringScale
                        )
                        drawCircle(
                            color = TvAccentBlue.copy(alpha = 0.28f),
                            radius = maxR * (0.7f + animatedRms * 0.45f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) TvAccentRed else Color(0xFF334155)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TvAccentRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = statusText,
                        color = TvAccentGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (liveTranscript.isNotBlank()) {
                        "\"$liveTranscript\""
                    } else {
                        stringResource(R.string.voice_search_example_hint)
                    },
                    color = if (liveTranscript.isNotBlank()) TvTextPrimary else TvTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (liveTranscript.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Center-Right: Multi-bar Real-Time Equalizer Sound Waves
        if (isListening) {
            Canvas(
                modifier = Modifier
                    .width(150.dp)
                    .height(44.dp)
                    .padding(horizontal = 12.dp)
            ) {
                val barCount = 14
                val totalW = size.width
                val maxH = size.height
                val barWidth = (totalW / (barCount * 1.8f)).coerceAtLeast(3.dp.toPx())
                val gap = (totalW - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

                for (i in 0 until barCount) {
                    val waveFactor = abs(sin(wavePhase + i * 0.55f))
                    val heightFraction = (0.18f + waveFactor * (0.35f + animatedRms * 0.65f)).coerceIn(0.15f, 1f)
                    val barH = maxH * heightFraction
                    val x = i * (barWidth + gap)
                    val y = (maxH - barH) / 2f

                    val barColor = when {
                        i % 3 == 0 -> TvAccentGold
                        i % 2 == 0 -> TvAccentBlue
                        else -> Color(0xFFEC4899)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
        }

        // Stop / Toggle button
        TvFocusableCard(
            onClick = onStopClick,
            scaleUpRatio = 1.05f,
            modifier = Modifier.height(38.dp)
        ) { isFocused ->
            Box(
                modifier = Modifier
                    .background(
                        if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SearchResultAppTile(
    app: AppItem,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.04f,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocused) TvAccentGold.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.65f),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBmp = remember(app.icon, app.banner) {
                (app.icon ?: app.banner)?.let { drawableToBitmapSafe(it) }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when (app.packageName) {
                    "com.minidownloader.portal" -> Image(painterResource(R.drawable.ic_minidownloader_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    "com.aurastore.portal" -> Image(painterResource(R.drawable.ic_aurastore_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    "com.miniyube.portal" -> Image(painterResource(R.drawable.ic_minitube_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    "com.crazygames.portal" -> Image(painterResource(R.drawable.ic_crazygames_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    "com.poki.portal" -> Image(painterResource(R.drawable.ic_poki_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    "com.playhop.portal" -> Image(painterResource(R.drawable.ic_playhop_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    "com.easygames.portal" -> Image(painterResource(R.drawable.ic_easygames_banner), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else -> {
                        if (iconBmp != null) {
                            Image(
                                bitmap = iconBmp.asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier.size(34.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = if (app.isGame) Icons.Default.SportsEsports else Icons.Default.Apps,
                                contentDescription = null,
                                tint = TvAccentGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = TvTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (app.isGame) stringResource(R.string.title_games) else stringResource(R.string.title_apps),
                    color = TvAccentGold,
                    fontSize = 10.sp
                )
            }

            if (app.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TvAccentGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultChannelTile(
    channel: IptvChannel,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.04f,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocused) TvAccentBlue.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.65f),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = TvAccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    color = TvTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.groupTitle.ifBlank { "Live TV" },
                    color = TvTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
