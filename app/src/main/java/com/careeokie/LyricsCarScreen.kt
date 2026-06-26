package com.careeokie

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LyricsCarScreen(carContext: CarContext) : Screen(carContext) {

    init {
        lifecycleScope.launch { NowPlayingState.track.collect { invalidate() } }
        lifecycleScope.launch { NowPlayingState.lyrics.collect { invalidate() } }
        lifecycleScope.launch { NowPlayingState.currentLineIndex.collect { invalidate() } }
        lifecycleScope.launch { NowPlayingState.isLoading.collect { invalidate() } }
    }

    override fun onGetTemplate(): Template {
        val track = NowPlayingState.track.value
        val lyrics = NowPlayingState.lyrics.value
        val lineIndex = NowPlayingState.currentLineIndex.value
        val isLoading = NowPlayingState.isLoading.value

        val message = when {
            track == null ->
                "No song detected.\n\nPlay something in YouTube Music."
            isLoading ->
                "${track.artist} — ${track.title}\n\nFetching lyrics…"
            lyrics == null ->
                "${track.artist} — ${track.title}\n\nLyrics not found.\nUse Search to find them manually."
            lyrics.syncedLines != null -> {
                val lines = lyrics.syncedLines
                buildString {
                    append("${track.artist} — ${track.title}\n\n")
                    val start = maxOf(0, lineIndex - 1)
                    val end = minOf(lines.size - 1, lineIndex + 3)
                    for (i in start..end) {
                        append(if (i == lineIndex) "▶  ${lines[i].text}" else "    ${lines[i].text}")
                        if (i < end) append("\n")
                    }
                }
            }
            lyrics.plainText != null ->
                "${track.artist} — ${track.title}\n\n${lyrics.plainText.take(600)}"
            else ->
                "No lyrics available."
        }

        val searchAction = Action.Builder()
            .setTitle("Search")
            .setOnClickListener { screenManager.push(SearchCarScreen(carContext)) }
            .build()

        return MessageTemplate.Builder(message)
            .setTitle("Careeokie")
            .addAction(searchAction)
            .build()
    }
}
