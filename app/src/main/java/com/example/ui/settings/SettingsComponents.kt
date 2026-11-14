package com.example.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun SettingsGlassCard(
    modifier: Modifier = Modifier,
    highlightColor: Color = Color.White,
    highlightActive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF151F35).copy(alpha = 0.88f),
                        Color(0xFF0D1426).copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (highlightActive) highlightColor.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.09f),
                shape = shape
            )
    ) {
        // Specular top glass highlight line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (highlightActive) highlightColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun SettingsCardHeader(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = TvAccentGold
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f))
                .border(
                    width = 1.dp,
                    color = iconTint.copy(alpha = 0.30f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TvTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TvTextSecondary,
                    fontSize = 12.5.sp
                )
            }
        }
    }
}

@Composable
fun SettingActionTile(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.025f,
        shape = RoundedCornerShape(14.dp),
        useDefaultBorder = false,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    ) { isFocused ->
        val bgColor by animateColorAsState(
            targetValue = if (isFocused) TvAccentGold else Color(0xFF131D33).copy(alpha = 0.85f),
            animationSpec = tween(150),
            label = "action_tile_bg"
        )
        val borderColor by animateColorAsState(
            targetValue = if (isFocused) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.09f),
            animationSpec = tween(150),
            label = "action_tile_border"
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isFocused) Color.Black.copy(alpha = 0.14f)
                        else TvAccentGold.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else TvAccentGoldRadiant,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    color = if (isFocused) Color.Black.copy(alpha = 0.72f) else TvTextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isFocused) Color.Black else TvTextSecondary.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingToggleTile(
    title: String,
    desc: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onToggle,
        scaleUpRatio = 1.02f,
        shape = RoundedCornerShape(16.dp),
        useDefaultBorder = false,
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
    ) { isFocused ->
        val bgColor by animateColorAsState(
            targetValue = if (isFocused) Color(0xFF1D2A45) else Color(0xFF131D33).copy(alpha = 0.85f),
            animationSpec = tween(150),
            label = "toggle_bg"
        )
        val borderColor by animateColorAsState(
            targetValue = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.09f),
            animationSpec = tween(150),
            label = "toggle_border"
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) TvAccentGoldRadiant else TvTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = TvAccentGold,
                    uncheckedThumbColor = TvTextSecondary,
                    uncheckedTrackColor = Color(0xFF0F172A)
                )
            )
        }
    }
}

@Composable
fun DefaultLauncherManagementCard(
    isDefault: Boolean,
    onEnable: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGlassCard(
        modifier = modifier,
        highlightColor = if (isDefault) Color(0xFF34D399) else TvAccentGold,
        highlightActive = isDefault
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDefault) TvAccentGold.copy(alpha = 0.16f)
                            else Color(0xFF334155).copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDefault) TvAccentGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = if (isDefault) TvAccentGold else TvTextSecondary,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_default_launcher_card_title),
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_default_launcher_status_desc),
                        color = TvTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isDefault) Color(0xFF065F46).copy(alpha = 0.75f)
                        else Color(0xFF475569).copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDefault) Color(0xFF34D399) else Color(0xFF94A3B8).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDefault) Color(0xFF34D399) else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDefault) stringResource(R.string.settings_default_launcher_active)
                        else stringResource(R.string.settings_default_launcher_not_active),
                        color = if (isDefault) Color(0xFFD1FAE5) else TvTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TvActionButton(
            text = stringResource(R.string.settings_set_default_launcher),
            onClick = onEnable,
            icon = Icons.Default.CheckCircle,
            baseColor = if (isDefault) Color(0xFF0F766E).copy(alpha = 0.75f) else TvAccentGold,
            focusedColor = TvAccentGold,
            fillWidth = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_set_default_launcher_desc),
            color = TvTextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun VoiceAssistantManagementCard(
    isDefaultAssistant: Boolean,
    onEnable: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGlassCard(
        modifier = modifier,
        highlightColor = if (isDefaultAssistant) Color(0xFF34D399) else TvAccentGold,
        highlightActive = isDefaultAssistant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDefaultAssistant) TvAccentGold.copy(alpha = 0.16f)
                            else Color(0xFF334155).copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDefaultAssistant) TvAccentGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isDefaultAssistant) TvAccentGold else TvTextSecondary,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_voice_assist_title),
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_voice_assist_desc),
                        color = TvTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isDefaultAssistant) Color(0xFF065F46).copy(alpha = 0.75f)
                        else Color(0xFF475569).copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDefaultAssistant) Color(0xFF34D399) else Color(0xFF94A3B8).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDefaultAssistant) Color(0xFF34D399) else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDefaultAssistant) stringResource(R.string.settings_voice_assist_active)
                        else stringResource(R.string.settings_voice_assist_not_active),
                        color = if (isDefaultAssistant) Color(0xFFD1FAE5) else TvTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TvActionButton(
            text = stringResource(R.string.settings_voice_assist_set_btn),
            onClick = onEnable,
            icon = Icons.Default.Mic,
            baseColor = if (isDefaultAssistant) Color(0xFF0F766E).copy(alpha = 0.75f) else TvAccentGold,
            focusedColor = TvAccentGold,
            fillWidth = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_voice_assist_hint),
            color = TvTextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun FilePermissionManagementCard(
    hasPermission: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGlassCard(
        modifier = modifier,
        highlightColor = if (hasPermission) Color(0xFF10B981) else TvAccentGold,
        highlightActive = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (hasPermission) Color(0xFF10B981).copy(alpha = 0.16f)
                            else TvAccentGold.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (hasPermission) Color(0xFF10B981) else TvAccentGold,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_file_access_title),
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_file_access_desc),
                        color = TvTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (hasPermission) Color(0xFF065F46).copy(alpha = 0.75f)
                        else TvAccentGold.copy(alpha = 0.18f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (hasPermission) Color(0xFF34D399) else TvAccentGold.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (hasPermission) stringResource(R.string.settings_file_access_granted)
                    else stringResource(R.string.settings_file_access_not_granted),
                    color = if (hasPermission) Color(0xFFD1FAE5) else TvAccentGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!hasPermission) {
            Spacer(modifier = Modifier.height(14.dp))
            TvActionButton(
                text = stringResource(R.string.settings_file_access_btn),
                onClick = onGrant,
                icon = Icons.Default.Folder,
                baseColor = TvAccentGold,
                focusedColor = TvAccentGold,
                fillWidth = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun <T> TvSelectField(
    label: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedIndex = options.indexOfFirst { it.first == selectedOption }.coerceAtLeast(0)
    val currentLabel = options.getOrNull(selectedIndex)?.second ?: ""

    TvFocusableCard(
        onClick = { showDialog = true },
        scaleUpRatio = 1.025f,
        shape = RoundedCornerShape(13.dp),
        useDefaultBorder = false,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) { isFocused ->
        val bgColor by animateColorAsState(
            targetValue = if (isFocused) TvAccentGold else Color(0xFF0C1324).copy(alpha = 0.92f),
            animationSpec = tween(150),
            label = "select_bg"
        )
        val borderColor by animateColorAsState(
            targetValue = if (isFocused) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.12f),
            animationSpec = tween(150),
            label = "select_border"
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = if (isFocused) Color.Black else TvTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isFocused) Color.Black.copy(alpha = 0.14f)
                            else TvAccentGold.copy(alpha = 0.14f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentLabel,
                        color = if (isFocused) Color.Black else TvAccentGoldRadiant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else TvTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    if (showDialog) {
        val selectedFocusRequester = remember { FocusRequester() }
        LaunchedEffect(showDialog) {
            try {
                selectedFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }

        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.58f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent,
                    shadowElevation = 28.dp,
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                TvAccentGold.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.16f),
                                TvAccentBlue.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .widthIn(min = 340.dp, max = 440.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF141E35),
                                        TvMidnightBackground
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Text(
                            text = label,
                            color = TvTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(TvAccentGold.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(options) { idx, (value, optionLabel) ->
                                val isSelected = (value == selectedOption)
                                val itemMod = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)

                                TvFocusableCard(
                                    onClick = {
                                        onOptionSelected(value)
                                        showDialog = false
                                    },
                                    focusRequester = if (idx == selectedIndex) selectedFocusRequester else remember { FocusRequester() },
                                    scaleUpRatio = 1.03f,
                                    shape = RoundedCornerShape(12.dp),
                                    useDefaultBorder = false,
                                    modifier = itemMod
                                ) { itemFocused ->
                                    val rowBg by animateColorAsState(
                                        targetValue = when {
                                            itemFocused -> TvAccentGold
                                            isSelected -> TvAccentGold.copy(alpha = 0.16f)
                                            else -> Color(0xFF16213A).copy(alpha = 0.6f)
                                        },
                                        animationSpec = tween(140),
                                        label = "opt_bg"
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(rowBg)
                                            .border(
                                                width = if (itemFocused) 2.dp else 1.dp,
                                                color = when {
                                                    itemFocused -> Color.White
                                                    isSelected -> TvAccentGold.copy(alpha = 0.45f)
                                                    else -> Color.White.copy(alpha = 0.06f)
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = optionLabel,
                                            color = when {
                                                itemFocused -> Color.Black
                                                isSelected -> TvAccentGoldRadiant
                                                else -> TvTextPrimary
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected || itemFocused) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (itemFocused) Color.Black else TvAccentGoldRadiant,
                                                modifier = Modifier.size(19.dp)
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

@Composable
fun TvModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.05f,
        shape = RoundedCornerShape(12.dp),
        useDefaultBorder = false,
        modifier = modifier.height(48.dp)
    ) { isFocused ->
        val bgColor by animateColorAsState(
            targetValue = when {
                isFocused -> TvAccentGold
                isSelected -> TvAccentGold.copy(alpha = 0.88f)
                else -> Color(0xFF1A253B)
            },
            animationSpec = tween(140),
            label = "mode_btn_bg"
        )
        val textColor = if (isFocused || isSelected) Color.Black else TvTextPrimary

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else if (isSelected) TvAccentGold else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TvActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    baseColor: Color = Color(0xFF23304A),
    focusedColor: Color = TvAccentGold,
    fillWidth: Boolean = false
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.04f,
        shape = RoundedCornerShape(12.dp),
        useDefaultBorder = false,
        modifier = modifier.height(46.dp)
    ) { isFocused ->
        val bgColor by animateColorAsState(
            targetValue = if (isFocused) focusedColor else baseColor,
            animationSpec = tween(140),
            label = "action_btn_bg"
        )
        val isBrightBg = bgColor.luminance() > 0.45f
        val contentColor = if (isBrightBg) Color.Black else Color.White

        Row(
            modifier = (if (fillWidth) Modifier.fillMaxSize() else Modifier.fillMaxHeight().wrapContentWidth())
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
