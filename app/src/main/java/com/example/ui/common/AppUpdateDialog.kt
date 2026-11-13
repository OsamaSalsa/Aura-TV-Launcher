package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.BuildConfig
import com.example.R
import com.example.data.update.AppUpdateInfo
import com.example.data.update.AppUpdateManager
import com.example.data.update.UpdateDownloadState
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLayoutDir = LocalLayoutDirection.current
    val currentConfig = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val primaryFocusRequester = remember { FocusRequester() }

    var downloadState by remember(updateInfo.versionCode, updateInfo.versionName) {
        mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle)
    }
    var waitingForInstallPermission by remember { mutableStateOf(false) }

    fun startDownloadAndInstall() {
        if (!AppUpdateManager.canInstallPackages(context)) {
            waitingForInstallPermission = true
            downloadState = UpdateDownloadState.PermissionRequired
            AppUpdateManager.openUnknownSourcesSettings(context)
            return
        }
        waitingForInstallPermission = false
        scope.launch {
            downloadState = UpdateDownloadState.Downloading(progress = 0f, downloadedMb = 0f, totalMb = 0f)
            val result = AppUpdateManager.downloadUpdateApk(
                context = context,
                apkUrl = updateInfo.apkUrl,
                expectedSha256 = updateInfo.sha256,
                onProgress = { progress, downloadedMb, totalMb ->
                    downloadState = UpdateDownloadState.Downloading(progress, downloadedMb, totalMb)
                }
            )
            result.fold(
                onSuccess = { apkFile ->
                    downloadState = UpdateDownloadState.ReadyToInstall(apkFile)
                    val launched = AppUpdateManager.installApk(context, apkFile)
                    if (!launched) {
                        downloadState = UpdateDownloadState.Error(
                            context.getString(R.string.update_install_launch_failed)
                        )
                    }
                },
                onFailure = { err ->
                    downloadState = UpdateDownloadState.Error(
                        err.message ?: context.getString(R.string.update_download_failed)
                    )
                }
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, waitingForInstallPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && waitingForInstallPermission) {
                if (AppUpdateManager.canInstallPackages(context)) {
                    waitingForInstallPermission = false
                    startDownloadAndInstall()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        try {
            primaryFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    val isDownloading = downloadState is UpdateDownloadState.Downloading

    Dialog(
        onDismissRequest = {
            if (!updateInfo.mandatory && !isDownloading) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.mandatory && !isDownloading,
            dismissOnClickOutside = !updateInfo.mandatory && !isDownloading
        )
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides currentConfig,
            LocalLayoutDirection provides currentLayoutDir
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = TvMidnightBackground.copy(alpha = 0.97f),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .width(500.dp)
                    .padding(16.dp)
                    .border(1.5.dp, TvAccentGold.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(TvAccentGold.copy(alpha = 0.18f))
                                .border(1.dp, TvAccentGold.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = stringResource(R.string.update_dialog_title),
                                tint = TvAccentGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.update_dialog_title),
                                color = TvTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}  →  v${updateInfo.versionName}",
                                color = TvAccentGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Changelog card
                    val notes = updateInfo.changelog.ifBlank {
                        stringResource(R.string.update_default_changelog)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = notes,
                            color = TvTextSecondary,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status / Progress area
                    when (val state = downloadState) {
                        is UpdateDownloadState.Downloading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val pctText = if (state.progress >= 0f) {
                                    "${(state.progress * 100).toInt()}%"
                                } else {
                                    "..."
                                }
                                val sizeText = if (state.totalMb > 0f) {
                                    String.format(Locale.US, "%.1f / %.1f MB", state.downloadedMb, state.totalMb)
                                } else {
                                    String.format(Locale.US, "%.1f MB", state.downloadedMb)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${stringResource(R.string.update_downloading)} ($pctText)",
                                        color = TvTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = sizeText,
                                        color = TvAccentGold,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (state.progress >= 0f) {
                                    LinearProgressIndicator(
                                        progress = { state.progress },
                                        color = TvAccentGold,
                                        trackColor = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        color = TvAccentGold,
                                        trackColor = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }

                        UpdateDownloadState.PermissionRequired -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TvAccentBlue.copy(alpha = 0.16f))
                                    .border(1.dp, TvAccentBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Default.Security, null, tint = TvAccentGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.update_permission_hint),
                                    color = TvTextPrimary,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        is UpdateDownloadState.ReadyToInstall -> {
                            Text(
                                text = stringResource(R.string.update_ready_to_install),
                                color = TvAccentGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        is UpdateDownloadState.Error -> {
                            Text(
                                text = state.message,
                                color = TvAccentRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        UpdateDownloadState.Idle -> Unit
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val readyFile: File? = (downloadState as? UpdateDownloadState.ReadyToInstall)?.apkFile
                        val primaryLabel = when (downloadState) {
                            is UpdateDownloadState.Downloading -> stringResource(R.string.update_downloading)
                            UpdateDownloadState.PermissionRequired -> stringResource(R.string.update_open_permission_settings)
                            is UpdateDownloadState.ReadyToInstall -> stringResource(R.string.update_install_now)
                            is UpdateDownloadState.Error -> stringResource(R.string.action_retry)
                            UpdateDownloadState.Idle -> stringResource(R.string.update_now)
                        }

                        TvFocusableCard(
                            onClick = {
                                if (readyFile != null) {
                                    AppUpdateManager.installApk(context, readyFile)
                                } else if (!isDownloading) {
                                    startDownloadAndInstall()
                                }
                            },
                            focusRequester = primaryFocusRequester,
                            scaleUpRatio = 1.04f,
                            useDefaultBorder = false,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isFocused) TvAccentGold else TvAccentGold.copy(alpha = 0.22f))
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = TvAccentGold,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = primaryLabel,
                                        tint = if (isFocused) Color.Black else TvAccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = primaryLabel,
                                        color = if (isFocused) Color.Black else TvTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (!updateInfo.mandatory && !isDownloading) {
                            TvFocusableCard(
                                onClick = onDismiss,
                                scaleUpRatio = 1.04f,
                                useDefaultBorder = false,
                                modifier = Modifier
                                    .width(128.dp)
                                    .height(48.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isFocused) Color.White.copy(alpha = 0.2f)
                                            else Color(0xFF151F35).copy(alpha = 0.8f)
                                        )
                                        .border(
                                            width = if (isFocused) 1.5.dp else 1.dp,
                                            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.action_later),
                                            tint = TvTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.action_later),
                                            color = TvTextPrimary,
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
        }
    }
}
