package com.careeokie

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.*

class MediaListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private var currentController: MediaController? = null
    private var lastTrack: TrackInfo? = null

    // Called whenever any app posts a notification.
    // We check if it carries a MediaSession token (i.e. it's a media player).
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val token = sbn.notification.extras
            .getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION) ?: return

        val controller = try {
            MediaController(applicationContext, token)
        } catch (e: Exception) {
            return
        }

        val metadata = controller.metadata ?: return
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: return
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return

        val track = TrackInfo(artist, title)
        currentController = controller

        // Only fetch new lyrics when the track actually changes
        if (track != lastTrack) {
            lastTrack = track
            NowPlayingState.track.value = track
            NowPlayingState.currentLineIndex.value = -1
            NowPlayingState.lyrics.value = null
            fetchLyrics(artist, title)
        }
    }

    override fun onListenerConnected() {
        startPositionPolling()
    }

    override fun onListenerDisconnected() {
        scope.cancel()
    }

    private fun fetchLyrics(artist: String, title: String) {
        scope.launch {
            NowPlayingState.isLoading.value = true
            NowPlayingState.error.value = null
            val result = LyricsRepository.fetch(artist, title)
            NowPlayingState.lyrics.value = result
            NowPlayingState.isLoading.value = false
            if (result == null) {
                NowPlayingState.error.value = "Lyrics not found — try searching manually"
            }
        }
    }

    // Polls playback position every 500ms to keep the highlighted line in sync.
    private fun startPositionPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                updateCurrentLine()
                delay(500)
            }
        }
    }

    private fun updateCurrentLine() {
        val controller = currentController ?: return
        val state = controller.playbackState ?: return
        if (state.state != PlaybackState.STATE_PLAYING) return

        val lines = NowPlayingState.lyrics.value?.syncedLines ?: return
        if (lines.isEmpty()) return

        // Calculate accurate real-time position from last reported state
        val elapsed = SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
        val positionMs = state.position + (elapsed * state.playbackSpeed).toLong()

        // Find the last line whose timestamp is at or before the current position
        val index = lines.indexOfLast { it.timeMs <= positionMs }

        if (index != NowPlayingState.currentLineIndex.value) {
            NowPlayingState.currentLineIndex.value = index
        }
    }
}
