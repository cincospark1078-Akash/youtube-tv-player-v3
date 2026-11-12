package com.example.tvplayer

import android.view.KeyEvent
import androidx.media3.exoplayer.ExoPlayer

/**
 * PlayerUIController
 *
 * Dedicated controller class inspired by SmartTube's architecture.
 * Encapsulates Android TV D-pad navigation and playback actions:
 *
 * - KEYCODE_MEDIA_PLAY_PAUSE: Toggles player.playWhenReady
 * - KEYCODE_DPAD_LEFT: Jumps backwards by 10,000ms (clamped to 0)
 * - KEYCODE_DPAD_RIGHT: Jumps forward by 10,000ms (clamped to duration)
 */
class PlayerUIController(private val player: ExoPlayer) {

    companion object {
        const val SEEK_OFFSET_MS = 10_000L
    }

    /**
     * Handles a dispatchKeyEvent from the host Activity or focused PlayerView.
     * @return true if the key event was consumed by the player controller.
     */
    fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        return when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                togglePlayPause()
                true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                seekBackward(SEEK_OFFSET_MS)
                true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                seekForward(SEEK_OFFSET_MS)
                true
            }

            else -> false
        }
    }

    fun togglePlayPause() {
        player.playWhenReady = !player.playWhenReady
    }

    fun seekBackward(offsetMs: Long = SEEK_OFFSET_MS) {
        val currentPosition = player.currentPosition
        val targetPosition = (currentPosition - offsetMs).coerceAtLeast(0L)
        player.seekTo(targetPosition)
    }

    fun seekForward(offsetMs: Long = SEEK_OFFSET_MS) {
        val currentPosition = player.currentPosition
        val duration = if (player.duration > 0) player.duration else Long.MAX_VALUE
        val targetPosition = (currentPosition + offsetMs).coerceAtMost(duration)
        player.seekTo(targetPosition)
    }
}
