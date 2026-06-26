package com.careeokie

object LrcParser {

    // Matches lines like: [01:23.45] Some lyric text
    // Also handles 3-digit milliseconds: [01:23.456]
    private val LINE_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")

    fun parse(lrc: String): List<LrcLine> {
        return lrc.lines()
            .mapNotNull { line ->
                LINE_REGEX.matchEntire(line.trim())?.let { match ->
                    val min = match.groupValues[1].toLong()
                    val sec = match.groupValues[2].toLong()
                    val msStr = match.groupValues[3]
                    // Normalise 2-digit ms to actual ms (e.g. "45" → 450ms)
                    val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                    val text = match.groupValues[4].trim()
                    LrcLine(min * 60_000 + sec * 1_000 + ms, text)
                }
            }
            .filter { it.text.isNotEmpty() }   // drop blank timestamp-only lines
            .sortedBy { it.timeMs }
    }
}
