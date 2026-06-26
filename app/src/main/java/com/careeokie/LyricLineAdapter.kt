package com.careeokie

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LyricLineAdapter : RecyclerView.Adapter<LyricLineAdapter.VH>() {

    private var lines: List<String> = emptyList()
    private var currentIndex: Int = -1

    fun setLines(newLines: List<String>) {
        lines = newLines
        notifyDataSetChanged()
    }

    fun setCurrentLine(index: Int) {
        val previous = currentIndex
        currentIndex = index
        // Refresh a window of lines around old and new positions
        val start = minOf(previous, index).coerceAtLeast(0)
        val end = maxOf(previous, index).coerceAtMost(lines.size - 1)
        for (i in start..end) notifyItemChanged(i)
    }

    override fun getItemCount() = lines.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lyric_line, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(lines[position], position - currentIndex)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLine: TextView = itemView.findViewById(R.id.tvLyricLine)
        private val viewBar: View = itemView.findViewById(R.id.viewAccentBar)

        fun bind(text: String, relativePos: Int) {
            tvLine.text = text
            when {
                relativePos < -1 -> {           // Lines already sung, fading out
                    tvLine.textSize = 12f
                    tvLine.setTextColor(Color.parseColor("#38FFFFFF"))
                    tvLine.typeface = Typeface.DEFAULT
                    viewBar.visibility = View.INVISIBLE
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
                relativePos == -1 -> {          // One line ago — still somewhat visible
                    tvLine.textSize = 14f
                    tvLine.setTextColor(Color.parseColor("#66FFFFFF"))
                    tvLine.typeface = Typeface.DEFAULT
                    viewBar.visibility = View.INVISIBLE
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
                relativePos == 0 -> {           // Current line — bright, bold, highlighted
                    tvLine.textSize = 20f
                    tvLine.setTextColor(Color.WHITE)
                    tvLine.typeface = Typeface.DEFAULT_BOLD
                    viewBar.visibility = View.VISIBLE
                    itemView.setBackgroundColor(Color.parseColor("#1E1040"))
                }
                relativePos == 1 -> {           // Next line — coming up soon
                    tvLine.textSize = 15f
                    tvLine.setTextColor(Color.parseColor("#8CFFFFFF"))
                    tvLine.typeface = Typeface.DEFAULT
                    viewBar.visibility = View.INVISIBLE
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
                else -> {                       // Upcoming lines — dimmed
                    tvLine.textSize = 12f
                    tvLine.setTextColor(Color.parseColor("#47FFFFFF"))
                    tvLine.typeface = Typeface.DEFAULT
                    viewBar.visibility = View.INVISIBLE
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }
}
