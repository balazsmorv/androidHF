package hu.bme.aut.android.hf.recyclerViewThings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.hf.R
import hu.bme.aut.android.hf.data.MovieData
import hu.bme.aut.android.hf.data.SearchResult

class HistoryResultAdapter(private val listener: HistoryResultClickListener) :
    RecyclerView.Adapter<HistoryResultAdapter.HistoryResultViewHolder>() {

    private val items = mutableListOf<MovieData>()

    fun addItem(item: MovieData) {
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
    ): HistoryResultViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.result_row, parent, false)
        return HistoryResultViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: HistoryResultViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.titleView.text = item.title
        holder.yearView.text = item.year

        holder.item = item


    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface HistoryResultClickListener {
        fun onItemChanged(item: MovieData)
        fun onTap(item: MovieData)
    }

    inner class HistoryResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView
        val yearView: TextView
        var item: MovieData? = null

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