package com.careeokie

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

data class SearchResult(val artist: String, val title: String)

object LyricsRepository {

    private val client = OkHttpClient()

    // Fetch lyrics for a known artist + title.
    // Tries LRCLIB first (synced LRC), falls back to Lyrics.ovh (plain text).
    suspend fun fetch(artist: String, title: String): LyricsResult? =
        withContext(Dispatchers.IO) {
            fetchLrcLib(artist, title) ?: fetchLyricsOvh(artist, title)
        }

    // Search LRCLIB by query string, returns a list of matching tracks.
    suspend fun search(query: String): List<SearchResult> =
        withContext(Dispatchers.IO) {
            searchLrcLib(query)
        }

    // --- Private helpers (blocking, must be called from IO dispatcher) ---

    private fun fetchLrcLib(artist: String, title: String): LyricsResult? {
        val url = "https://lrclib.net/api/get" +
                "?artist_name=${artist.encode()}&track_name=${title.encode()}"
        val body = get(url) ?: return null
        return try {
            val json = JSONObject(body)
            val synced = json.optString("syncedLyrics").takeIf { it.isNotEmpty() }
            val plain = json.optString("plainLyrics").takeIf { it.isNotEmpty() }
            when {
                synced != null -> LyricsResult(LrcParser.parse(synced), plain)
                plain != null -> LyricsResult(null, plain)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchLyricsOvh(artist: String, title: String): LyricsResult? {
        val url = "https://api.lyrics.ovh/v1/${artist.encode()}/${title.encode()}"
        val body = get(url) ?: return null
        return try {
            val lyrics = JSONObject(body).optString("lyrics").takeIf { it.isNotEmpty() }
            lyrics?.let { LyricsResult(null, it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun searchLrcLib(query: String): List<SearchResult> {
        val url = "https://lrclib.net/api/search?q=${query.encode()}"
        val body = get(url) ?: return emptyList()
        return try {
            val arr = JSONArray(body)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val artist = obj.optString("artistName").takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val title = obj.optString("trackName").takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                // Skip pure instrumental tracks — no lyrics to show
                if (obj.optBoolean("instrumental", false)) return@mapNotNull null
                SearchResult(artist, title)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun get(url: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (e: Exception) {
            null
        }
    }

    private fun String.encode() = URLEncoder.encode(this, "UTF-8")
}
