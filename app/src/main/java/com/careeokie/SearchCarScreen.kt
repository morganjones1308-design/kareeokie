package com.careeokie

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import kotlinx.coroutines.*

class SearchCarScreen(carContext: CarContext) : Screen(carContext) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var results: List<SearchResult> = emptyList()
    private var isSearching = false

    override fun onGetTemplate(): Template {
        val itemList = ItemList.Builder().apply {
            when {
                isSearching -> setNoItemsMessage("Searching…")
                results.isEmpty() -> setNoItemsMessage("Type a song or artist name and press Search")
                else -> results.forEach { result ->
                    addItem(
                        Row.Builder()
                            .setTitle(result.title)
                            .addText(result.artist)
                            .setOnClickListener { selectResult(result) }
                            .build()
                    )
                }
            }
        }.build()

        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchTextChanged(text: String) { /* live search not needed */ }
                override fun onSearchSubmitted(text: String) {
                    if (text.isNotBlank()) performSearch(text)
                }
            }
        )
            .setShowKeyboardByDefault(true)
            .setHeaderAction(Action.BACK)
            .setItemList(itemList)
            .build()
    }

    private fun performSearch(query: String) {
        isSearching = true
        invalidate()
        scope.launch {
            val r = LyricsRepository.search(query)
            withContext(Dispatchers.Main) {
                results = r
                isSearching = false
                invalidate()
            }
        }
    }

    private fun selectResult(result: SearchResult) {
        NowPlayingState.track.value = TrackInfo(result.artist, result.title)
        NowPlayingState.lyrics.value = null
        NowPlayingState.currentLineIndex.value = -1
        scope.launch {
            NowPlayingState.isLoading.value = true
            val lyrics = LyricsRepository.fetch(result.artist, result.title)
            NowPlayingState.lyrics.value = lyrics
            NowPlayingState.isLoading.value = false
            if (lyrics == null) {
                NowPlayingState.error.value = "Lyrics not found"
            }
        }
        screenManager.pop()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
