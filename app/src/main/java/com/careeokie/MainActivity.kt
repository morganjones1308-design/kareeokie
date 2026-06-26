package com.careeokie

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.careeokie.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LyricLineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LyricLineAdapter()
        binding.rvLyrics.layoutManager = LinearLayoutManager(this)
        binding.rvLyrics.adapter = adapter

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.btnNotificationAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        observeState()
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        val listeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val granted = listeners?.contains(packageName) == true
        binding.layoutPermission.visibility = if (granted) View.GONE else View.VISIBLE
    }

    private fun observeState() {
        lifecycleScope.launch {
            NowPlayingState.track.collectLatest { track ->
                if (track != null) {
                    // Bullet prefix gives the pill a polished "now playing" feel
                    binding.tvSongTitle.text = "● ${track.artist} — ${track.title}"
                    binding.tvSongTitle.visibility = View.VISIBLE
                    binding.tvNoSong.visibility = View.GONE
                } else {
                    binding.tvSongTitle.visibility = View.GONE
                    binding.tvNoSong.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            NowPlayingState.isLoading.collectLatest { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            NowPlayingState.error.collectLatest { error ->
                binding.tvError.text = error ?: ""
                binding.tvError.visibility = if (error != null) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            NowPlayingState.lyrics.collectLatest { result ->
                when {
                    result?.syncedLines != null -> {
                        adapter.setLines(result.syncedLines.map { it.text })
                        binding.rvLyrics.visibility = View.VISIBLE
                        binding.scrollLyricsPlain.visibility = View.GONE
                    }
                    result?.plainText != null -> {
                        binding.tvLyricsPlain.text = result.plainText
                        binding.scrollLyricsPlain.visibility = View.VISIBLE
                        binding.rvLyrics.visibility = View.GONE
                    }
                    else -> {
                        binding.rvLyrics.visibility = View.GONE
                        binding.scrollLyricsPlain.visibility = View.GONE
                    }
                }
            }
        }

        lifecycleScope.launch {
            NowPlayingState.currentLineIndex.collectLatest { index ->
                adapter.setCurrentLine(index)
                if (index >= 0) {
                    binding.rvLyrics.smoothScrollToPosition(index)
                }
            }
        }
    }
}
