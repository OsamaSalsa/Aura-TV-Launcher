package com.example.ui.settings

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class AppStorageEntry(
    val packageName: String,
    val label: String,
    val sizeBytes: Long
)

private sealed interface StorageUiState {
    data object Loading : StorageUiState
    data class Success(val entries: List<AppStorageEntry>) : StorageUiState
    data class Error(val message: String) : StorageUiState
}

@Composable
fun AppStorageManagerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf<StorageUiState>(StorageUiState.Loading) }
    var reloadNonce by remember { mutableStateOf(0) }

    LaunchedEffect(reloadNonce) {
        uiState = StorageUiState.Loading
        uiState = try {
            val loaded = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val ownPkg = context.packageName
                @Suppress("DEPRECATION")
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                apps.mapNotNull { info ->
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                        (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                    if (isSystem || info.packageName == ownPkg) return@mapNotNull null
                    val label = try {
                        pm.getApplicationLabel(info).toString()
                    } catch (_: Exception) {
                        info.packageName
                    }
                    val apkSize = try {
                        val path = info.publicSourceDir ?: info.sourceDir
                        if (path != null) File(path).length() else 0L
                    } catch (_: Exception) {
                        0L
                    }
                    AppStorageEntry(
                        packageName = info.packageName,
                        label = label,
                        sizeBytes = apkSize
                    )
                }.sortedByDescending { it.sizeBytes }
            }
            StorageUiState.Success(loaded)
        } catch (e: Exception) {
            StorageUiState.Error(e.message ?: "Failed to scan apps")
        }
    }

    AppStorageManagerDialogContent(
        uiState = uiState,
        onOpenAppInfo = { pkg ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", pkg, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        },
        onUninstallApp = { pkg ->
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$pkg")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        },
        onRetry = { reloadNonce++ },
        onDismiss = onDismiss
    )
}

@Composable
private fun AppStorageManagerDialogContent(
    uiState: StorageUiState,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0A0F1D))
                .border(1.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.storage_manager_title),
                                color = TvTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.storage_manager_desc),
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("storage_close_button"),
                        shape = RoundedCornerShape(12.dp),
                        cardBackgroundColor = Color.White.copy(alpha = 0.08f)
                    ) { isFocused ->
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = if (isFocused) TvAccentGold else TvTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                when (uiState) {
                    StorageUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TvAccentGold)
                        }
                    }

                    is StorageUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.message,
                                color = TvAccentRed,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TvFocusableCard(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                cardBackgroundColor = TvAccentGold.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = stringResource(R.string.action_retry),
                                    color = TvTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    is StorageUiState.Success -> {
                        if (uiState.entries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.storage_manager_empty),
                                    color = TvTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 380.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.entries, key = { it.packageName }) { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = entry.label,
                                                color = TvTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${entry.packageName} • ${formatBytes(entry.sizeBytes)}",
                                                color = TvTextSecondary,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            TvFocusableCard(
                                                onClick = { onOpenAppInfo(entry.packageName) },
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("storage_info_${entry.packageName}"),
                                                shape = RoundedCornerShape(12.dp),
                                                cardBackgroundColor = Color.White.copy(alpha = 0.08f)
                                            ) { isFocused ->
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = stringResource(R.string.action_info),
                                                        tint = if (isFocused) TvAccentGold else TvTextPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = stringResource(R.string.action_info),
                                                        color = TvTextPrimary,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }

                                            TvFocusableCard(
                                                onClick = { onUninstallApp(entry.packageName) },
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("storage_uninstall_${entry.packageName}"),
                                                shape = RoundedCornerShape(12.dp),
                                                cardBackgroundColor = TvAccentRed.copy(alpha = 0.14f)
                                            ) { isFocused ->
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = stringResource(R.string.action_uninstall),
                                                        tint = if (isFocused) TvAccentGold else TvAccentRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = stringResource(R.string.action_uninstall),
                                                        color = TvTextPrimary,
                                                        fontSize = 12.sp
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
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return if (mb >= 1024.0) {
        String.format(Locale.US, "%.2f GB", mb / 1024.0)
    } else {
        String.format(Locale.US, "%.1f MB", mb)
    }
}

@Preview(widthDp = 960, heightDp = 540)
@Composable
private fun AppStorageManagerPreview() {
    MyApplicationTheme {
        Surface(color = Color.Black) {
            AppStorageManagerDialogContent(
                uiState = StorageUiState.Success(
                    listOf(
                        AppStorageEntry("com.example.stream", "Stream Player", 142_000_000L),
                        AppStorageEntry("com.example.browser", "TV Browser", 48_500_000L)
                    )
                ),
                onOpenAppInfo = {},
                onUninstallApp = {},
                onRetry = {},
                onDismiss = {}
            )
        }
    }
}
