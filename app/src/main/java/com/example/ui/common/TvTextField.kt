package com.example.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Android TV friendly text input field:
 * - While browsing with the D-Pad, acts as a focusable glass card and NEVER opens the keyboard.
 * - Only enters text-editing mode and shows the on-screen keyboard after pressing OK/Enter.
 * - Pressing Done or Back cleanly closes the keyboard and returns D-Pad focus to the field.
 */
@Composable
fun TvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    val cardFocusRequester = remember { FocusRequester() }
    val inputFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    fun exitEditAndKeepFocusHere() {
        if (!isEditing) return
        isEditing = false
        keyboardController?.hide()
        scope.launch {
            delay(40)
            try {
                cardFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    fun exitEditOnFocusLost() {
        if (!isEditing) return
        isEditing = false
        keyboardController?.hide()
    }

    if (isEditing) {
        BackHandler(enabled = true) {
            exitEditAndKeepFocusHere()
        }

        LaunchedEffect(Unit) {
            delay(50)
            try {
                inputFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }

        var inputHadFocus by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = TvTextSecondary,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TvAccentGold
                    )
                }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { exitEditAndKeepFocusHere() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TvAccentGold,
                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                focusedTextColor = TvTextPrimary,
                unfocusedTextColor = TvTextPrimary,
                focusedContainerColor = Color(0xFF0A101F),
                unfocusedContainerColor = Color(0xFF0A101F),
                cursorColor = TvAccentGold
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .heightIn(min = 52.dp)
                .focusRequester(inputFocusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        inputHadFocus = true
                    } else if (inputHadFocus) {
                        exitEditOnFocusLost()
                    }
                }
        )
    } else {
        TvFocusableCard(
            onClick = { isEditing = true },
            focusRequester = cardFocusRequester,
            scaleUpRatio = 1.02f,
            shape = RoundedCornerShape(12.dp),
            useDefaultBorder = false,
            modifier = modifier.height(54.dp)
        ) { isFocused ->
            val bgColor by animateColorAsState(
                targetValue = if (isFocused) Color(0xFF141E35) else Color(0xFF0A101F),
                animationSpec = tween(140),
                label = "tv_tf_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.22f),
                animationSpec = tween(140),
                label = "tv_tf_border"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) TvAccentGoldRadiant else TvAccentGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = value.ifBlank { placeholder },
                    color = if (value.isNotBlank()) TvTextPrimary else TvTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (value.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (isFocused) TvAccentGoldRadiant else TvTextSecondary.copy(alpha = 0.55f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
