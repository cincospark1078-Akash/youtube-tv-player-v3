package com.example.tvplayer

import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Android TV Fullscreen ExoPlayer Activity
 *
 * Implements fullscreen video playback without UI toolbars, requests focus
 * for the player view, and intercepts D-pad key events via dispatchKeyEvent
 * (inspired by SmartTube's PlayerUIController).
 */
class MainActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    // Public test video stream (Big Buck Bunny 1080p MP4)
    private val testVideoUrl =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep the TV screen on during video playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system bars for complete fullscreen immersion
        hideSystemBars()

        // Initialize ExoPlayer using Media3
        initializePlayer()

        setContent {
            val context = LocalContext.current

            // Retain PlayerView reference and bind to ExoPlayer
            val pView = remember {
                PlayerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Disable built-in UI toolbars/controls: pure fullscreen player
                    useController = false

                    // Bind initialized ExoPlayer
                    this.player = this@MainActivity.player

                    // Request focus so that TV key events are directed here
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()
                }.also {
                    playerView = it
                }
            }

            // Cleanup when leaving composition
            DisposableEffect(Unit) {
                onDispose {
                    pView.player = null
                }
            }

            AndroidView(
                factory = { pView },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(this)
                .build()
                .apply {
                    val mediaItem = MediaItem.fromUri(testVideoUrl)
                    setMediaItem(mediaItem)
                    repeatMode = Player.REPEAT_MODE_ALL
                    prepare()
                    playWhenReady = true
                }
        }
    }

    /**
     * Override dispatchKeyEvent to handle TV remote / D-pad keys.
     * Logic inspired by SmartTube's PlayerUIController:
     * - KEYCODE_MEDIA_PLAY_PAUSE (and D-pad Center): toggle play/pause
     * - KEYCODE_DPAD_LEFT: seek back 10,000ms (clamped to 0)
     * - KEYCODE_DPAD_RIGHT: seek forward 10,000ms (clamped to duration)
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val exoPlayer = player ?: return super.dispatchKeyEvent(event)

        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    // Toggle playWhenReady
                    exoPlayer.playWhenReady = !exoPlayer.playWhenReady
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // Seek back 10,000 ms, not less than 0
                    val currentPos = exoPlayer.currentPosition
                    val targetPos = (currentPos - 10000L).coerceAtLeast(0L)
                    exoPlayer.seekTo(targetPos)
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    // Seek forward 10,000 ms, not more than player.duration
                    val currentPos = exoPlayer.currentPosition
                    val duration = if (exoPlayer.duration > 0) exoPlayer.duration else Long.MAX_VALUE
                    val targetPos = (currentPos + 10000L).coerceAtMost(duration)
                    exoPlayer.seekTo(targetPos)
                    return true
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        playerView?.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        playerView = null
    }
}
