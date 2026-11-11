package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.model.IptvChannel
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun ChannelActionDialog(
    channel: IptvChannel,
    isReordering: Boolean = false,
    onDismiss: () -> Unit,
    onMove: () -> Unit,
    onConfirmMove: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    val dialogContext = androidx.compose.ui.platform.LocalContext.current
    val currentLayoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val currentConfiguration = androidx.compose.ui.platform.LocalConfiguration.current

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
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Channel Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (channel.logoUrl.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(channel.logoUrl),
                            contentDescription = channel.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, null, tint = TvAccentGold)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = channel.name,
                            color = TvTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = channel.groupTitle,
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
                        ActionMenuItem(
                            icon = Icons.Default.Check,
                            title = stringResource(R.string.action_confirm_move),
                            onClick = onConfirmMove
                        )
                    } else {
                        ActionMenuItem(
                            icon = Icons.Default.OpenWith,
                            title = stringResource(R.string.action_move),
                            onClick = onMove
                        )
                    }

                    ActionMenuItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.iptv_toggle_favorite),
                        tint = TvAccentRed,
                        onClick = onRemoveFavorite
                    )

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
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.04f,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isFocused) TvTextPrimary else tint,
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
