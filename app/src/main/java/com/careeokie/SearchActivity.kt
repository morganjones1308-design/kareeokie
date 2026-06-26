package com.careeokie

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.careeokie.databinding.ActivitySearchBinding
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SearchResultAdapter { result -> loadLyrics(result) }
        binding.rvResults.layoutManager = LinearLayoutManager(this)
        binding.rvResults.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) performSearch(query)
        }

        // Allow pressing "Search" on the keyboard to trigger search
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) performSearch(query)
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvNoResults.visibility = View.GONE
        lifecycleScope.launch {
            val results = LyricsRepository.search(query)
            binding.progressBar.visibility = View.GONE
            adapter.setResults(results)
            binding.tvNoResults.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun loadLyrics(result: SearchResult) {
        // Push the chosen track into shared state — MediaListener won't override this
        // until the next song actually changes on the media session.
        NowPlayingState.track.value = TrackInfo(result.artist, result.title)
        NowPlayingState.lyrics.value = null
        NowPlayingState.currentLineIndex.value = -1
        NowPlayingState.error.value = null

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val lyrics = LyricsRepository.fetch(result.artist, result.title)
            NowPlayingState.lyrics.value = lyrics
            NowPlayingState.isLoading.value = false
            if (lyrics == null) {
                NowPlayingState.error.value = "Lyrics not found for this track"
            }
            binding.progressBar.visibility = View.GONE
            finish()
        }
    }
}
