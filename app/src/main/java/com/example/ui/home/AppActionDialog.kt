package com.example.ui.home

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.model.AppItem
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import com.example.ui.common.drawableToBitmapSafe

@Composable
fun AppActionDialog(
    app: AppItem,
    isReordering: Boolean = false,
    onDismiss: () -> Unit,
    onOpen: (() -> Unit)? = null,
    onMove: () -> Unit,
    onConfirmMove: () -> Unit,
    onToggleGameCategory: () -> Unit,
    onToggleLock: () -> Unit = {},
    onHide: () -> Unit,
    onInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    val dialogContext = androidx.compose.ui.platform.LocalContext.current
    val currentLayoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val currentConfiguration = androidx.compose.ui.platform.LocalConfiguration.current
    val firstActionFocusRequester = remember { FocusRequester() }
    val isBuiltInPortal = remember(app.packageName) { app.packageName.endsWith(".portal") }

    LaunchedEffect(Unit) {
        try {
            firstActionFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalContext provides dialogContext,
            androidx.compose.ui.platform.LocalConfiguration provides currentConfiguration,
            androidx.compose.ui.platform.LocalLayoutDirection provides currentLayoutDirection
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = TvMidnightBackground.copy(alpha = 0.96f),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .width(440.dp)
                    .padding(16.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Icon & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val bmp = (app.banner ?: app.icon)?.let { drawableToBitmapSafe(it) }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(width = 68.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = app.label,
                                color = TvTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = app.packageName,
                                color = TvTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isReordering) {
                            // Confirm Move
                            ActionMenuItem(
                                icon = Icons.Default.Check,
                                title = stringResource(R.string.action_confirm_move),
                                focusRequester = firstActionFocusRequester,
                                onClick = onConfirmMove
                            )
                        } else {
                            if (onOpen != null) {
                                ActionMenuItem(
                                    icon = Icons.Default.PlayArrow,
                                    title = stringResource(R.string.action_open),
                                    focusRequester = firstActionFocusRequester,
                                    onClick = onOpen
                                )
                            }
                            // Move
                            ActionMenuItem(
                                icon = Icons.Default.OpenWith,
                                title = stringResource(R.string.action_move),
                                focusRequester = if (onOpen == null) firstActionFocusRequester else null,
                                onClick = onMove
                            )
                        }

                        // Move to Games / Move to Apps
                        ActionMenuItem(
                            icon = Icons.Default.SportsEsports,
                            title = stringResource(if (app.isGame) R.string.action_move_to_apps else R.string.action_move_to_games),
                            onClick = onToggleGameCategory
                        )
                        // Lock / Unlock with PIN
                        ActionMenuItem(
                            icon = if (app.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            title = stringResource(if (app.isLocked) R.string.action_unlock_app else R.string.action_lock_app),
                            onClick = onToggleLock
                        )
                        // Hide
                        ActionMenuItem(
                            icon = Icons.Default.VisibilityOff,
                            title = stringResource(R.string.action_hide),
                            onClick = onHide
                        )
                        if (!isBuiltInPortal) {
                            // App Info
                            ActionMenuItem(
                                icon = Icons.Default.Info,
                                title = stringResource(R.string.action_info),
                                onClick = onInfo
                            )
                            // Uninstall
                            ActionMenuItem(
                                icon = Icons.Default.Delete,
                                title = stringResource(R.string.action_uninstall),
                                tint = TvAccentRed,
                                onClick = onUninstall
                            )
                        }
                        // Cancel
                        ActionMenuItem(
                            icon = Icons.Default.Close,
                            title = stringResource(R.string.action_cancel),
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: androidx.compose.ui.graphics.Color = TvAccentGold,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    val itemFocusRequester = focusRequester ?: remember { FocusRequester() }
    TvFocusableCard(
        onClick = onClick,
        focusRequester = itemFocusRequester,
        scaleUpRatio = 1.03f,
        useDefaultBorder = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isFocused) TvAccentGold.copy(alpha = 0.22f)
                    else Color(0xFF151F35).copy(alpha = 0.72f)
                )
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isFocused) TvAccentGold else tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = if (isFocused) TvTextPrimary else TvTextSecondary,
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

