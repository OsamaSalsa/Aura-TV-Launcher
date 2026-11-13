package com.example.ui.common

import android.view.KeyEvent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant

private val FastFocusTween = tween<Float>(durationMillis = 110, easing = FastOutSlowInEasing)
private val FocusedGlowColor = TvAccentGoldRadiant.copy(alpha = 0.75f)
private val UnfocusedBorderColor = Color.White.copy(alpha = 0.08f)

fun isRemoteMenuKey(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_MENU ||
        keyCode == KeyEvent.KEYCODE_TV_CONTENTS_MENU ||
        keyCode == KeyEvent.KEYCODE_TV_MEDIA_CONTEXT_MENU ||
        keyCode == KeyEvent.KEYCODE_SETTINGS ||
        keyCode == KeyEvent.KEYCODE_BOOKMARK ||
        keyCode == 82 ||
        keyCode == 174 ||
        keyCode == 176 ||
        keyCode == 256 ||
        keyCode == 257
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() },
    shape: Shape = RoundedCornerShape(16.dp),
    scaleUpRatio: Float = 1.12f,
    elevation: Dp = 14.dp,
    cardBackgroundColor: Color? = null,
    useDefaultBorder: Boolean = true,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var longClickTriggered by remember { mutableStateOf(false) }

    val scaleState = animateFloatAsState(
        targetValue = if (isFocused) scaleUpRatio else 1.0f,
        animationSpec = FastFocusTween,
        label = "tv_card_scale"
    )

    val currentBackgroundColor = when {
        isFocused && cardBackgroundColor != null -> cardBackgroundColor
        isFocused -> Color.Transparent
        else -> cardBackgroundColor ?: Color.Transparent
    }

    val currentBorderColor = if (isFocused) TvAccentGold else UnfocusedBorderColor

    Box(
        modifier = modifier
            .zIndex(if (isFocused) 2f else 0f)
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .onFocusChanged { focusState ->
                val nowFocused = focusState.isFocused
                if (isFocused != nowFocused) {
                    isFocused = nowFocused
                    if (!nowFocused) {
                        longClickTriggered = false
                    } else {
                        onFocused?.invoke()
                    }
                }
            }
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (isFocused && onLongClick != null && isRemoteMenuKey(native.keyCode)) {
                    if (native.action == KeyEvent.ACTION_DOWN) {
                        if (native.repeatCount == 0) {
                            onLongClick()
                        }
                        return@onKeyEvent true
                    } else if (native.action == KeyEvent.ACTION_UP) {
                        return@onKeyEvent true
                    }
                }
                if (onLongClick != null && isFocused) {
                    val isCenterOrEnter = native.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        native.keyCode == KeyEvent.KEYCODE_ENTER ||
                        native.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                    if (isCenterOrEnter) {
                        if (native.action == KeyEvent.ACTION_DOWN) {
                            if (native.repeatCount >= 1 && !longClickTriggered) {
                                longClickTriggered = true
                                onLongClick()
                                return@onKeyEvent true
                            }
                            if (longClickTriggered) {
                                return@onKeyEvent true
                            }
                        } else if (native.action == KeyEvent.ACTION_UP) {
                            if (longClickTriggered) {
                                longClickTriggered = false
                                return@onKeyEvent true
                            }
                        }
                    }
                }
                false
            }
            .focusRequester(focusRequester)
            .focusable()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(
                if (isFocused && elevation > 0.dp && cardBackgroundColor != Color.Transparent) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape,
                        ambientColor = FocusedGlowColor,
                        spotColor = FocusedGlowColor
                    )
                } else Modifier
            )
            .clip(shape)
            .background(currentBackgroundColor)
            .then(
                if (useDefaultBorder) {
                    Modifier.border(
                        width = if (isFocused) 2.5.dp else 1.dp,
                        color = currentBorderColor,
                        shape = shape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content(isFocused)
    }
}
