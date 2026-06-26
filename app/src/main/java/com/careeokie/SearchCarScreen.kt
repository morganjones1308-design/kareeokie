package com.careeokie

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SearchCarScreen(carContext: CarContext) : Screen(carContext) {

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
                override fun onSearchTextChanged(text: String) {}
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
        lifecycleScope.launch {
            results = LyricsRepository.search(query)
            isSearching = false
            invalidate()
        }
    }

    private fun selectResult(result: SearchResult) {
        NowPlayingState.track.value = TrackInfo(result.artist, result.title)
        NowPlayingState.lyrics.value = null
        NowPlayingState.currentLineIndex.value = -1
        lifecycleScope.launch {
            NowPlayingState.isLoading.value = true
            val lyrics = LyricsRepository.fetch(result.artist, result.title)
            NowPlayingState.lyrics.value = lyrics
            NowPlayingState.isLoading.value = false
            if (lyrics == null) NowPlayingState.error.value = "Lyrics not found"
        }
        screenManager.pop()
    }
}
