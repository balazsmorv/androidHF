package hu.bme.aut.android.hf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import hu.bme.aut.android.hf.data.MovieData
import hu.bme.aut.android.hf.data.MovieDetailsDatabase
import hu.bme.aut.android.hf.recyclerViewThings.HistoryResultAdapter
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity(), HistoryResultAdapter.HistoryResultClickListener {

    private lateinit var database: MovieDetailsDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        database = Room.databaseBuilder(applicationContext, MovieDetailsDatabase::class.java, "movies").build()
        initRecyclerView()
    }

    override fun onStop() {
        super.onStop()
        database.close()
    }

    override fun onItemChanged(item: MovieData) {
        TODO("Not yet implemented")
    }

    override fun onTap(item: MovieData) {
        TODO("Not yet implemented")
    }

    private fun initRecyclerView() {
        recyclerView = historyTableView
        adapter = HistoryResultAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
