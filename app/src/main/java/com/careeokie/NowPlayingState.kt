package com.careeokie

import kotlinx.coroutines.flow.MutableStateFlow

// Timestamps + text for a single synced lyric line
data class LrcLine(val timeMs: Long, val text: String)

// Result from the lyrics API — either synced lines, plain text, or both
data class LyricsResult(
    val syncedLines: List<LrcLine>?,  // null if no LRC available
    val plainText: String?
)

// Currently detected song
data class TrackInfo(val artist: String, val title: String)

// Single source of truth shared across MediaListener, MainActivity, and Car screens.
// All fields are MutableStateFlow so observers get updates automatically.
object NowPlayingState {
    val track = MutableStateFlow<TrackInfo?>(null)
    val lyrics = MutableStateFlow<LyricsResult?>(null)
    val currentLineIndex = MutableStateFlow(-1)
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
}
