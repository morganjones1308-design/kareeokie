package com.careeokie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchResultAdapter(
    private val onSelect: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.VH>() {

    private var results: List<SearchResult> = emptyList()

    fun setResults(newResults: List<SearchResult>) {
        results = newResults
        notifyDataSetChanged()
    }

    override fun getItemCount() = results.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(results[position], onSelect)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvResultTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvResultArtist)

        fun bind(result: SearchResult, onSelect: (SearchResult) -> Unit) {
            tvTitle.text = result.title
            tvArtist.text = result.artist
            itemView.setOnClickListener { onSelect(result) }
        }
    }
}
