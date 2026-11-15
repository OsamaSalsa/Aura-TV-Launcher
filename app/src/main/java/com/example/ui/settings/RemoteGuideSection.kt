package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

private data class RemoteShortcutGuideItem(
    val icon: ImageVector,
    val accent: Color,
    val titleRes: Int,
    val whenRes: Int,
    val descRes: Int
)

@Composable
fun RemoteGuideSection() {
    val shortcutItems = listOf(
        RemoteShortcutGuideItem(
            icon = Icons.Default.Gamepad,
            accent = TvAccentGold,
            titleRes = R.string.remote_btn_dpad_title,
            whenRes = R.string.remote_btn_dpad_when,
            descRes = R.string.remote_btn_dpad_desc
        ),
        RemoteShortcutGuideItem(
            icon = Icons.Default.CheckCircle,
            accent = TvAccentBlue,
            titleRes = R.string.remote_btn_ok_title,
            whenRes = R.string.remote_btn_ok_when,
            descRes = R.string.remote_btn_ok_desc
        ),
        RemoteShortcutGuideItem(
            icon = Icons.Default.TouchApp,
            accent = Color(0xFFA78BFA),
            titleRes = R.string.remote_btn_ok_long_title,
            whenRes = R.string.remote_btn_ok_long_when,
            descRes = R.string.remote_btn_ok_long_desc
        ),
        RemoteShortcutGuideItem(
            icon = Icons.Default.Mic,
            accent = Color(0xFF34D399),
            titleRes = R.string.remote_btn_assistant_title,
            whenRes = R.string.remote_btn_assistant_when,
            descRes = R.string.remote_btn_assistant_desc
        ),
        RemoteShortcutGuideItem(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            accent = Color(0xFF38BDF8),
            titleRes = R.string.remote_btn_back_title,
            whenRes = R.string.remote_btn_back_when,
            descRes = R.string.remote_btn_back_desc
        ),
        RemoteShortcutGuideItem(
            icon = Icons.Default.LiveTv,
            accent = Color(0xFFF472B6),
            titleRes = R.string.remote_btn_iptv_player_title,
            whenRes = R.string.remote_btn_iptv_player_when,
            descRes = R.string.remote_btn_iptv_player_desc
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Intro Overview Header Card
        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.SettingsRemote,
                    title = stringResource(R.string.settings_remote_intro_title),
                    subtitle = stringResource(R.string.settings_remote_intro_desc)
                )
            }
        }

        // Featured Spotlight Card: Three-Lines (Menu) Button — What & When
        item {
            TvFocusableCard(
                onClick = {},
                scaleUpRatio = 1.015f,
                shape = RoundedCornerShape(18.dp),
                cardBackgroundColor = Color.Transparent,
                elevation = 0.dp,
                useDefaultBorder = false,
                modifier = Modifier.fillMaxWidth()
            ) { isFocused ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF19233C).copy(alpha = if (isFocused) 0.98f else 0.88f),
                                    Color(0xFF0E1629).copy(alpha = 0.94f)
                                )
                            )
                        )
                        .border(
                            width = if (isFocused) 2.dp else 1.25.dp,
                            color = if (isFocused) TvAccentGold else TvAccentGold.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(TvAccentGold.copy(alpha = 0.22f))
                                    .border(1.dp, TvAccentGold.copy(alpha = 0.65f), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.remote_menu_btn_title),
                                    tint = TvAccentGoldRadiant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.remote_menu_btn_title),
                                    color = TvTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.remote_menu_btn_subtitle),
                                    color = TvAccentGoldRadiant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(TvAccentGold.copy(alpha = 0.18f))
                                .border(1.dp, TvAccentGold.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.remote_menu_btn_badge),
                                color = TvAccentGoldRadiant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MenuContextRuleRow(
                            icon = Icons.Default.TouchApp,
                            accent = TvAccentGold,
                            whenTitle = stringResource(R.string.remote_menu_when_home_card_title),
                            description = stringResource(R.string.remote_menu_when_home_card_desc)
                        )
                        MenuContextRuleRow(
                            icon = Icons.Default.Gamepad,
                            accent = TvAccentBlue,
                            whenTitle = stringResource(R.string.remote_menu_when_home_top_title),
                            description = stringResource(R.string.remote_menu_when_home_top_desc)
                        )
                        MenuContextRuleRow(
                            icon = Icons.Default.LiveTv,
                            accent = Color(0xFF34D399),
                            whenTitle = stringResource(R.string.remote_menu_when_iptv_title),
                            description = stringResource(R.string.remote_menu_when_iptv_desc)
                        )
                        MenuContextRuleRow(
                            icon = Icons.Default.Settings,
                            accent = Color(0xFFA78BFA),
                            whenTitle = stringResource(R.string.remote_menu_when_settings_key_title),
                            description = stringResource(R.string.remote_menu_when_settings_key_desc)
                        )
                    }
                }
            }
        }

        // Standard Remote Buttons Guide Cards
        items(shortcutItems, key = { it.titleRes }) { item ->
            RemoteShortcutCard(item = item)
        }
    }
}

@Composable
private fun MenuContextRuleRow(
    icon: ImageVector,
    accent: Color,
    whenTitle: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A1020).copy(alpha = 0.72f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = whenTitle,
                color = TvTextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                color = TvTextSecondary,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RemoteShortcutCard(item: RemoteShortcutGuideItem) {
    TvFocusableCard(
        onClick = {},
        scaleUpRatio = 1.015f,
        shape = RoundedCornerShape(16.dp),
        cardBackgroundColor = Color.Transparent,
        elevation = 0.dp,
        useDefaultBorder = false,
        modifier = Modifier.fillMaxWidth()
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isFocused) Color(0xFF1A2540).copy(alpha = 0.96f)
                    else Color(0xFF10182B).copy(alpha = 0.85f)
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.accent.copy(alpha = 0.16f))
                    .border(1.dp, item.accent.copy(alpha = 0.38f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = stringResource(item.titleRes),
                    tint = item.accent,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(item.titleRes),
                        color = if (isFocused) TvAccentGoldRadiant else TvTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.accent.copy(alpha = 0.14f))
                            .border(1.dp, item.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(item.whenRes),
                            color = item.accent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(item.descRes),
                    color = TvTextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
