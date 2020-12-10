package hu.bme.aut.android.hf.recyclerViewThings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.hf.R
import hu.bme.aut.android.hf.data.SearchResult

class SearchResultAdapter(private val listener: SearchResultClickListener) :
        RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    private val items = mutableListOf<SearchResult>()

    fun addItem(item: SearchResult) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItems() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchResultViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.result_row, parent, false)
        return SearchResultViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: SearchResultViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.titleView.text = item.Title
        holder.yearView.text = item.Year

        holder.item = item


    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface SearchResultClickListener {
        fun onItemChanged(item: SearchResult)
        fun onTap(item: SearchResult)
    }

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView
        val yearView: TextView
        var item: SearchResult? = null

        init {
            titleView = itemView.findViewById(R.id.titleTextView)
            yearView = itemView.findViewById(R.id.yearTextView)
            itemView.setOnClickListener {
                item?.let {
                    listener.onItemChanged(it)
                    listener.onTap(it)
                }
            }
        }
    }

}