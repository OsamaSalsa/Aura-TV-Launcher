package com.example.ui.youtube

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.model.YouTubeVideo
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerDialog(
    video: YouTubeVideo,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Official Android YouTube Player (android-youtube-player library) bypassing 152-4 and embed restrictions
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    matchParent()
                    enableAutomaticInitialization = false
                    enableBackgroundPlayback(true)

                    fun configureChildWebViews(view: android.view.View) {
                        if (view is android.webkit.WebView) {
                            view.settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    safeBrowsingEnabled = false
                                }
                            }
                            view.webViewClient = object : android.webkit.WebViewClient() {
                                override fun onRenderProcessGone(v: android.webkit.WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                                    (v?.parent as? android.view.ViewGroup)?.removeView(v)
                                    try {
                                        v?.destroy()
                                    } catch (_: Exception) {}
                                    v?.postDelayed({
                                        onClose()
                                    }, 300)
                                    return true
                                }
                            }
                            view.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                        } else if (view is ViewGroup) {
                            for (i in 0 until view.childCount) {
                                configureChildWebViews(view.getChildAt(i))
                            }
                        }
                    }
                    configureChildWebViews(this)

                    val options = IFramePlayerOptions.Builder(ctx)
                        .controls(1)
                        .autoplay(1)
                        .rel(0)
                        .ivLoadPolicy(3)
                        .ccLoadPolicy(0)
                        .origin("https://${ctx.packageName}")
                        .build()

                    initialize(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadVideo(video.videoId, 0f)
                                youTubePlayer.play()
                            }
                        },
                        true,
                        options,
                        video.videoId
                    )
                }
            },
            onRelease = { ytView ->
                try {
                    ytView.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Header Overlay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .padding(horizontal = 48.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = TvTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = video.channelTitle,
                        color = TvAccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.youtube_player_hint),
                        color = TvTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            TvFocusableCard(
                onClick = onClose,
                modifier = Modifier.size(42.dp)
            ) { isFocused ->
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_cancel),
                    tint = if (isFocused) TvAccentGold else TvTextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
