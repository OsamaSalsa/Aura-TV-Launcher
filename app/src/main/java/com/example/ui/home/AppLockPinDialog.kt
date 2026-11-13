package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun AppLockPinDialog(
    title: String,
    subtitle: String,
    expectedPin: String,
    onPinVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val centerKeyFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            centerKeyFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    fun appendDigit(digit: String) {
        if (enteredPin.length < 4) {
            isError = false
            val next = enteredPin + digit
            enteredPin = next
            if (next.length == 4) {
                if (next == expectedPin) {
                    onPinVerified()
                } else {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

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
                shape = RoundedCornerShape(26.dp),
            color = TvMidnightBackground.copy(alpha = 0.98f),
            shadowElevation = 24.dp,
            modifier = Modifier
                .width(380.dp)
                .padding(16.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(26.dp))
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        val code = event.nativeKeyEvent.keyCode
                        val digit = when (code) {
                            android.view.KeyEvent.KEYCODE_0, android.view.KeyEvent.KEYCODE_NUMPAD_0 -> "0"
                            android.view.KeyEvent.KEYCODE_1, android.view.KeyEvent.KEYCODE_NUMPAD_1 -> "1"
                            android.view.KeyEvent.KEYCODE_2, android.view.KeyEvent.KEYCODE_NUMPAD_2 -> "2"
                            android.view.KeyEvent.KEYCODE_3, android.view.KeyEvent.KEYCODE_NUMPAD_3 -> "3"
                            android.view.KeyEvent.KEYCODE_4, android.view.KeyEvent.KEYCODE_NUMPAD_4 -> "4"
                            android.view.KeyEvent.KEYCODE_5, android.view.KeyEvent.KEYCODE_NUMPAD_5 -> "5"
                            android.view.KeyEvent.KEYCODE_6, android.view.KeyEvent.KEYCODE_NUMPAD_6 -> "6"
                            android.view.KeyEvent.KEYCODE_7, android.view.KeyEvent.KEYCODE_NUMPAD_7 -> "7"
                            android.view.KeyEvent.KEYCODE_8, android.view.KeyEvent.KEYCODE_NUMPAD_8 -> "8"
                            android.view.KeyEvent.KEYCODE_9, android.view.KeyEvent.KEYCODE_NUMPAD_9 -> "9"
                            else -> null
                        }
                        if (digit != null) {
                            appendDigit(digit)
                            true
                        } else if (code == android.view.KeyEvent.KEYCODE_DEL) {
                            if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                            true
                        } else false
                    } else false
                }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TvAccentGold.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = TvTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isError) stringResource(R.string.pin_wrong_error) else subtitle,
                    color = if (isError) TvAccentRed else TvTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                if (expectedPin == "1234" && !isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.pin_default_hint),
                        color = TvAccentGold.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        val filled = idx < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) TvAccentGold
                                    else if (isError) TvAccentRed.copy(alpha = 0.35f)
                                    else Color.White.copy(alpha = 0.16f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (filled) TvAccentGold else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Keypad Grid (3x4)
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                TvFocusableCard(
                                    onClick = {
                                        when (key) {
                                            "C" -> {
                                                enteredPin = ""
                                                isError = false
                                            }
                                            "⌫" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            }
                                            else -> appendDigit(key)
                                        }
                                    },
                                    focusRequester = if (key == "5") centerKeyFocusRequester else remember { FocusRequester() },
                                    scaleUpRatio = 1.06f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) { isFocused ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isFocused) TvAccentGold else Color(0xFF1E293B),
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (key == "⌫") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Backspace",
                                                tint = if (isFocused) Color.Black else TvTextPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                color = if (isFocused) Color.Black else TvTextPrimary,
                                                fontSize = 16.sp,
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
        }
    }
}
}
